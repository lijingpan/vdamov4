package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.entity.MemberEntity;
import com.vdamo.ordering.entity.OrderEntity;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.entity.TableEntity;
import com.vdamo.ordering.mapper.MemberMapper;
import com.vdamo.ordering.mapper.OrderMapper;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.mapper.TableMapper;
import com.vdamo.ordering.model.OrderSummary;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final StoreMapper storeMapper;
    private final TableMapper tableMapper;
    private final MemberMapper memberMapper;
    private final PermissionService permissionService;

    public OrderService(
            OrderMapper orderMapper,
            StoreMapper storeMapper,
            TableMapper tableMapper,
            MemberMapper memberMapper,
            PermissionService permissionService
    ) {
        this.orderMapper = orderMapper;
        this.storeMapper = storeMapper;
        this.tableMapper = tableMapper;
        this.memberMapper = memberMapper;
        this.permissionService = permissionService;
    }

    public List<OrderSummary> list(Long storeId) {
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
}
