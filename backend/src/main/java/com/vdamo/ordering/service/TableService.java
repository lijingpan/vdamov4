package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.entity.OrderEntity;
import com.vdamo.ordering.entity.TableEntity;
import com.vdamo.ordering.mapper.OrderMapper;
import com.vdamo.ordering.mapper.TableMapper;
import com.vdamo.ordering.model.TableCurrentOrderSummary;
import com.vdamo.ordering.model.TableSummary;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class TableService {

    private static final Set<String> OPEN_ORDER_STATUS = Set.of(
            "PENDING",
            "CONFIRMED",
            "IN_PROGRESS",
            "COOKING",
            "SERVED",
            "WAITING_CHECKOUT"
    );

    private final TableMapper tableMapper;
    private final OrderMapper orderMapper;
    private final PermissionService permissionService;

    public TableService(TableMapper tableMapper, OrderMapper orderMapper, PermissionService permissionService) {
        this.tableMapper = tableMapper;
        this.orderMapper = orderMapper;
        this.permissionService = permissionService;
    }

    public List<TableSummary> list(Long storeId) {
        Long targetStoreId = permissionService.resolveStoreId(storeId);

        List<TableEntity> tables = tableMapper.selectList(
                new LambdaQueryWrapper<TableEntity>()
                        .eq(TableEntity::getStoreId, targetStoreId)
                        .orderByAsc(TableEntity::getAreaName)
                        .orderByAsc(TableEntity::getTableName));
        if (tables.isEmpty()) {
            return List.of();
        }

        Map<Long, TableCurrentOrderSummary> currentOrderMap = buildCurrentOrderMap(targetStoreId);
        return tables.stream()
                .map(table -> new TableSummary(
                        table.getId(),
                        table.getStoreId(),
                        table.getAreaName(),
                        table.getTableName(),
                        table.getCapacity() == null ? 0 : table.getCapacity(),
                        table.getStatus(),
                        currentOrderMap.get(table.getId())))
                .toList();
    }

    private Map<Long, TableCurrentOrderSummary> buildCurrentOrderMap(Long storeId) {
        List<OrderEntity> openOrders = orderMapper.selectList(
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getStoreId, storeId)
                        .in(OrderEntity::getOrderStatus, OPEN_ORDER_STATUS)
                        .orderByDesc(OrderEntity::getCreateTime)
                        .orderByDesc(OrderEntity::getId));

        Map<Long, TableCurrentOrderSummary> summaryMap = new LinkedHashMap<>();
        for (OrderEntity order : openOrders) {
            summaryMap.putIfAbsent(order.getTableId(), new TableCurrentOrderSummary(
                    order.getId(),
                    order.getOrderNo(),
                    order.getOrderStatus(),
                    order.getPaymentStatus(),
                    order.getPayableAmountInCent() == null ? 0 : order.getPayableAmountInCent(),
                    order.getAppendCount() != null && order.getAppendCount() > 0));
        }
        return summaryMap;
    }
}
