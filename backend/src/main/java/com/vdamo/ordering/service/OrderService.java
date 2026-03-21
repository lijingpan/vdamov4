package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.common.exception.BadRequestException;
import com.vdamo.ordering.common.exception.NotFoundException;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.entity.MemberEntity;
import com.vdamo.ordering.entity.OrderAppendLogEntity;
import com.vdamo.ordering.entity.OrderEntity;
import com.vdamo.ordering.entity.OrderItemEntity;
import com.vdamo.ordering.entity.PaymentRecordEntity;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.entity.TableEntity;
import com.vdamo.ordering.mapper.MemberMapper;
import com.vdamo.ordering.mapper.OrderAppendLogMapper;
import com.vdamo.ordering.mapper.OrderItemMapper;
import com.vdamo.ordering.mapper.OrderMapper;
import com.vdamo.ordering.mapper.PaymentRecordMapper;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.mapper.TableMapper;
import com.vdamo.ordering.model.OrderDetailResponse;
import com.vdamo.ordering.model.OrderPaymentStatusUpdateRequest;
import com.vdamo.ordering.model.OrderStatusUpdateRequest;
import com.vdamo.ordering.model.OrderSummary;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OrderService {

    private static final Set<String> ORDER_STATUSES = Set.of(
            "PENDING_CONFIRM",
            "PENDING",
            "PLACED",
            "CONFIRMED",
            "IN_PROGRESS",
            "COOKING",
            "SERVED",
            "WAITING_CHECKOUT",
            "COMPLETED",
            "CANCELLED",
            "REFUNDED"
    );
    private static final Set<String> OPEN_ORDER_STATUSES = Set.of(
            "PENDING_CONFIRM",
            "PENDING",
            "PLACED",
            "CONFIRMED",
            "IN_PROGRESS",
            "COOKING",
            "SERVED"
    );
    private static final Set<String> PAYMENT_STATUSES = Set.of(
            "UNPAID",
            "PARTIAL",
            "PAID",
            "REFUNDED"
    );

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderAppendLogMapper orderAppendLogMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final StoreMapper storeMapper;
    private final TableMapper tableMapper;
    private final MemberMapper memberMapper;
    private final PermissionService permissionService;
    private final MessageHelper messageHelper;

    public OrderService(
            OrderMapper orderMapper,
            OrderItemMapper orderItemMapper,
            OrderAppendLogMapper orderAppendLogMapper,
            PaymentRecordMapper paymentRecordMapper,
            StoreMapper storeMapper,
            TableMapper tableMapper,
            MemberMapper memberMapper,
            PermissionService permissionService,
            MessageHelper messageHelper
    ) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderAppendLogMapper = orderAppendLogMapper;
        this.paymentRecordMapper = paymentRecordMapper;
        this.storeMapper = storeMapper;
        this.tableMapper = tableMapper;
        this.memberMapper = memberMapper;
        this.permissionService = permissionService;
        this.messageHelper = messageHelper;
    }

    public List<OrderSummary> list(Long storeId, String keyword, String orderStatus, String paymentStatus) {
        List<Long> storeScope = permissionService.currentStoreIds();
        if (storeId != null) {
            permissionService.assertStoreAccess(storeId);
        }

        LambdaQueryWrapper<OrderEntity> wrapper = new LambdaQueryWrapper<OrderEntity>()
                .in(OrderEntity::getStoreId, storeScope)
                .orderByDesc(OrderEntity::getCreateTime)
                .orderByDesc(OrderEntity::getId);
        if (storeId != null) {
            wrapper.eq(OrderEntity::getStoreId, storeId);
        }
        if (StringUtils.hasText(orderStatus)) {
            wrapper.eq(OrderEntity::getOrderStatus, orderStatus.trim());
        }
        if (StringUtils.hasText(paymentStatus)) {
            wrapper.eq(OrderEntity::getPaymentStatus, paymentStatus.trim());
        }
        if (StringUtils.hasText(keyword)) {
            String normalizedKeyword = keyword.trim();
            Long orderIdKeyword = parseOrderId(normalizedKeyword);
            wrapper.and(query -> {
                query.like(OrderEntity::getOrderNo, normalizedKeyword);
                if (orderIdKeyword != null) {
                    query.or().eq(OrderEntity::getId, orderIdKeyword);
                }
            });
        }

        List<OrderEntity> orders = orderMapper.selectList(wrapper);
        if (orders.isEmpty()) {
            return List.of();
        }

        Map<Long, String> storeNameMap = storeMapper
                .selectList(new LambdaQueryWrapper<StoreEntity>().in(StoreEntity::getId, storeScope))
                .stream()
                .collect(Collectors.toMap(StoreEntity::getId, StoreEntity::getName, (left, right) -> left));

        List<Long> tableIds = orders.stream().map(OrderEntity::getTableId).distinct().toList();
        Map<Long, String> tableNameMap = tableMapper
                .selectList(new LambdaQueryWrapper<TableEntity>().in(TableEntity::getId, tableIds))
                .stream()
                .collect(Collectors.toMap(TableEntity::getId, TableEntity::getTableName, (left, right) -> left));

        List<Long> memberIds = orders.stream()
                .map(OrderEntity::getMemberId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> memberNameMap = memberIds.isEmpty()
                ? Map.of()
                : memberMapper.selectList(new LambdaQueryWrapper<MemberEntity>().in(MemberEntity::getId, memberIds))
                        .stream()
                        .collect(Collectors.toMap(MemberEntity::getId, MemberEntity::getDisplayName, (left, right) -> left));

        return orders.stream()
                .map(order -> toSummary(order, storeNameMap, tableNameMap, memberNameMap))
                .toList();
    }

    public OrderDetailResponse getDetail(Long orderId) {
        OrderEntity order = requireOrder(orderId);
        permissionService.assertStoreAccess(order.getStoreId());

        StoreEntity store = storeMapper.selectById(order.getStoreId());
        TableEntity table = tableMapper.selectById(order.getTableId());
        MemberEntity member = order.getMemberId() == null ? null : memberMapper.selectById(order.getMemberId());

        List<OrderItemEntity> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItemEntity>()
                        .eq(OrderItemEntity::getOrderId, order.getId())
                        .eq(OrderItemEntity::getStoreId, order.getStoreId())
                        .orderByAsc(OrderItemEntity::getAppendRound)
                        .orderByAsc(OrderItemEntity::getId));

        List<OrderAppendLogEntity> appendLogs = orderAppendLogMapper.selectList(
                new LambdaQueryWrapper<OrderAppendLogEntity>()
                        .eq(OrderAppendLogEntity::getOrderId, order.getId())
                        .eq(OrderAppendLogEntity::getStoreId, order.getStoreId())
                        .orderByAsc(OrderAppendLogEntity::getAppendRound)
                        .orderByAsc(OrderAppendLogEntity::getId));

        List<PaymentRecordEntity> paymentRecords = paymentRecordMapper.selectList(
                new LambdaQueryWrapper<PaymentRecordEntity>()
                        .eq(PaymentRecordEntity::getOrderId, order.getId())
                        .eq(PaymentRecordEntity::getStoreId, order.getStoreId())
                        .orderByAsc(PaymentRecordEntity::getPaidTime)
                        .orderByAsc(PaymentRecordEntity::getId));

        List<OrderDetailResponse.Item> itemDetails = orderItems.stream()
                .map(item -> new OrderDetailResponse.Item(
                        item.getId(),
                        item.getProductId(),
                        item.getItemName(),
                        item.getItemCode(),
                        defaultInt(item.getUnitPriceInCent()),
                        defaultInt(item.getQuantity()),
                        defaultInt(item.getOriginalAmountInCent()),
                        defaultInt(item.getDiscountAmountInCent()),
                        defaultInt(item.getPayableAmountInCent()),
                        item.getItemStatus(),
                        defaultInt(item.getAppendRound()),
                        item.getRemark()))
                .toList();

        List<OrderDetailResponse.AppendLog> appendLogDetails = appendLogs.stream()
                .map(log -> new OrderDetailResponse.AppendLog(
                        log.getId(),
                        defaultInt(log.getAppendRound()),
                        log.getActionType(),
                        defaultInt(log.getAppendItemCount()),
                        defaultInt(log.getAppendAmountInCent()),
                        log.getOperateTime(),
                        log.getOperatorName(),
                        log.getNote()))
                .toList();

        List<OrderDetailResponse.PaymentRecord> paymentDetails = paymentRecords.stream()
                .map(payment -> new OrderDetailResponse.PaymentRecord(
                        payment.getId(),
                        payment.getPaymentNo(),
                        payment.getPaymentMethod(),
                        payment.getPaymentChannel(),
                        defaultInt(payment.getPaidAmountInCent()),
                        defaultInt(payment.getChangeAmountInCent()),
                        payment.getPaymentStatus(),
                        payment.getPaidTime(),
                        payment.getCashierName(),
                        payment.getRemark()))
                .toList();

        int itemPayableAmount = orderItems.stream()
                .map(OrderItemEntity::getPayableAmountInCent)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);

        int appendAmount = appendLogs.stream()
                .map(OrderAppendLogEntity::getAppendAmountInCent)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);

        int paymentRecordAmount = paymentRecords.stream()
                .map(PaymentRecordEntity::getPaidAmountInCent)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);

        int payableAmount = defaultInt(order.getPayableAmountInCent());
        int paidAmount = defaultInt(order.getPaidAmountInCent());

        return new OrderDetailResponse(
                new OrderDetailResponse.Header(
                        order.getId(),
                        order.getOrderNo(),
                        order.getStoreId(),
                        store == null ? "" : store.getName(),
                        order.getTableId(),
                        table == null ? "" : table.getTableName(),
                        order.getMemberId(),
                        member == null ? null : member.getDisplayName(),
                        order.getOrderStatus(),
                        order.getPaymentStatus(),
                        defaultInt(order.getAppendCount()),
                        order.getCreateTime()),
                itemDetails,
                appendLogDetails,
                paymentDetails,
                new OrderDetailResponse.AmountSummary(
                        defaultInt(order.getOriginalAmountInCent()),
                        defaultInt(order.getDiscountAmountInCent()),
                        payableAmount,
                        paidAmount,
                        Math.max(payableAmount - paidAmount, 0),
                        itemPayableAmount,
                        appendAmount,
                        paymentRecordAmount));
    }

    public OrderDetailResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        OrderEntity order = requireOrder(orderId);
        permissionService.assertStoreAccess(order.getStoreId());

        String targetStatus = normalizeOrderStatus(request.orderStatus());
        if ("COMPLETED".equals(targetStatus) && !"PAID".equals(order.getPaymentStatus())) {
            throw new BadRequestException("Order must be paid before marking completed");
        }

        order.setOrderStatus(targetStatus);
        order.setUpdater(permissionService.currentUser().username());
        orderMapper.updateById(order);
        syncTableStatusForOrder(order, targetStatus);
        return getDetail(orderId);
    }

    public OrderDetailResponse updatePaymentStatus(Long orderId, OrderPaymentStatusUpdateRequest request) {
        OrderEntity order = requireOrder(orderId);
        permissionService.assertStoreAccess(order.getStoreId());

        String targetPaymentStatus = normalizePaymentStatus(request.paymentStatus());
        order.setPaymentStatus(targetPaymentStatus);
        if ("PAID".equals(targetPaymentStatus)) {
            order.setPaidAmountInCent(defaultInt(order.getPayableAmountInCent()));
        } else if ("UNPAID".equals(targetPaymentStatus) || "REFUNDED".equals(targetPaymentStatus)) {
            order.setPaidAmountInCent(0);
        } else {
            order.setPaidAmountInCent(Math.min(defaultInt(order.getPaidAmountInCent()), defaultInt(order.getPayableAmountInCent())));
        }
        order.setUpdater(permissionService.currentUser().username());
        orderMapper.updateById(order);
        return getDetail(orderId);
    }

    public OrderDetailResponse completeOrder(Long orderId) {
        OrderEntity order = requireOrder(orderId);
        permissionService.assertStoreAccess(order.getStoreId());
        if (!"PAID".equals(order.getPaymentStatus())) {
            throw new BadRequestException("Order must be PAID before completion");
        }

        order.setOrderStatus("COMPLETED");
        order.setPaidAmountInCent(defaultInt(order.getPayableAmountInCent()));
        order.setUpdater(permissionService.currentUser().username());
        orderMapper.updateById(order);
        syncTableStatusForOrder(order, "COMPLETED");
        return getDetail(orderId);
    }

    private OrderSummary toSummary(
            OrderEntity entity,
            Map<Long, String> storeNameMap,
            Map<Long, String> tableNameMap,
            Map<Long, String> memberNameMap
    ) {
        return new OrderSummary(
                entity.getId(),
                entity.getOrderNo(),
                entity.getStoreId(),
                storeNameMap.getOrDefault(entity.getStoreId(), ""),
                entity.getTableId(),
                tableNameMap.getOrDefault(entity.getTableId(), ""),
                entity.getMemberId(),
                entity.getMemberId() == null ? null : memberNameMap.getOrDefault(entity.getMemberId(), ""),
                defaultInt(entity.getOriginalAmountInCent()),
                defaultInt(entity.getDiscountAmountInCent()),
                defaultInt(entity.getPayableAmountInCent()),
                defaultInt(entity.getPaidAmountInCent()),
                entity.getOrderStatus(),
                entity.getPaymentStatus(),
                defaultInt(entity.getAppendCount()) > 0,
                entity.getCreateTime()
        );
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String normalizeOrderStatus(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!ORDER_STATUSES.contains(normalized)) {
            throw new BadRequestException("Unsupported order status");
        }
        return normalized;
    }

    private String normalizePaymentStatus(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!PAYMENT_STATUSES.contains(normalized)) {
            throw new BadRequestException("Unsupported payment status");
        }
        return normalized;
    }

    private void syncTableStatusForOrder(OrderEntity order, String targetOrderStatus) {
        TableEntity table = tableMapper.selectById(order.getTableId());
        if (table == null || "DISABLED".equals(table.getStatus())) {
            return;
        }
        String nextTableStatus = resolveTableStatus(targetOrderStatus);
        if (!StringUtils.hasText(nextTableStatus) || nextTableStatus.equals(table.getStatus())) {
            return;
        }
        table.setStatus(nextTableStatus);
        table.setUpdater(permissionService.currentUser().username());
        tableMapper.updateById(table);
    }

    private String resolveTableStatus(String orderStatus) {
        if ("WAITING_CHECKOUT".equals(orderStatus)) {
            return "WAITING_CHECKOUT";
        }
        if ("COMPLETED".equals(orderStatus) || "CANCELLED".equals(orderStatus) || "REFUNDED".equals(orderStatus)) {
            return "IDLE";
        }
        if (OPEN_ORDER_STATUSES.contains(orderStatus)) {
            return "IN_USE";
        }
        return null;
    }

    private OrderEntity requireOrder(Long orderId) {
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new NotFoundException(messageHelper.get("error.order.notFound"));
        }
        return order;
    }

    private Long parseOrderId(String keyword) {
        try {
            return Long.parseLong(keyword);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
