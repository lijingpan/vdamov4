package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.common.exception.BadRequestException;
import com.vdamo.ordering.common.exception.NotFoundException;
import com.vdamo.ordering.common.support.IdGenerator;
import com.vdamo.ordering.entity.OrderEntity;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.entity.TableAreaEntity;
import com.vdamo.ordering.entity.TableEntity;
import com.vdamo.ordering.mapper.OrderMapper;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.mapper.TableAreaMapper;
import com.vdamo.ordering.mapper.TableMapper;
import com.vdamo.ordering.model.TableCurrentOrderSummary;
import com.vdamo.ordering.model.TableStatusUpdateRequest;
import com.vdamo.ordering.model.TableSummary;
import com.vdamo.ordering.model.TableUpsertRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    private static final Set<String> TABLE_STATUSES = Set.of(
            "IDLE",
            "IN_USE",
            "WAITING_CHECKOUT",
            "WAITING_CLEAN",
            "DISABLED"
    );

    private final TableMapper tableMapper;
    private final OrderMapper orderMapper;
    private final StoreMapper storeMapper;
    private final TableAreaMapper tableAreaMapper;
    private final PermissionService permissionService;
    private final IdGenerator idGenerator;

    public TableService(
            TableMapper tableMapper,
            OrderMapper orderMapper,
            StoreMapper storeMapper,
            TableAreaMapper tableAreaMapper,
            PermissionService permissionService,
            IdGenerator idGenerator
    ) {
        this.tableMapper = tableMapper;
        this.orderMapper = orderMapper;
        this.storeMapper = storeMapper;
        this.tableAreaMapper = tableAreaMapper;
        this.permissionService = permissionService;
        this.idGenerator = idGenerator;
    }

    public List<TableSummary> list(Long storeId, String status, String keyword) {
        List<Long> targetStoreIds = resolveStoreScope(storeId);
        Map<Long, String> storeNameMap = loadStoreNameMap(targetStoreIds);
        String normalizedStatus = StringUtils.hasText(status) ? normalizeStatus(status) : null;

        List<TableEntity> tables = tableMapper.selectList(
                new LambdaQueryWrapper<TableEntity>()
                        .in(TableEntity::getStoreId, targetStoreIds)
                        .eq(normalizedStatus != null, TableEntity::getStatus, normalizedStatus)
                        .and(StringUtils.hasText(keyword), query -> query
                                .like(TableEntity::getAreaName, keyword.trim())
                                .or()
                                .like(TableEntity::getTableName, keyword.trim()))
                        .orderByAsc(TableEntity::getStoreId)
                        .orderByAsc(TableEntity::getAreaName)
                        .orderByAsc(TableEntity::getTableName));
        if (tables.isEmpty()) {
            return List.of();
        }

        Map<Long, TableCurrentOrderSummary> currentOrderMap = buildCurrentOrderMap(targetStoreIds);
        return tables.stream()
                .map(table -> new TableSummary(
                        table.getId(),
                        table.getStoreId(),
                        storeNameMap.getOrDefault(table.getStoreId(), ""),
                        table.getAreaName(),
                        table.getTableName(),
                        table.getTableName(),
                        table.getCapacity() == null ? 0 : table.getCapacity(),
                        table.getStatus(),
                        currentOrderMap.get(table.getId())))
                .toList();
    }

    public TableSummary create(TableUpsertRequest request) {
        permissionService.assertStoreAccess(request.storeId());
        requireStore(request.storeId());
        requireArea(request.storeId(), request.areaName());

        TableEntity entity = new TableEntity();
        entity.setId(idGenerator.nextId());
        applyTableValues(entity, request);
        tableMapper.insert(entity);
        return getSummary(entity.getId());
    }

    public TableSummary update(Long id, TableUpsertRequest request) {
        permissionService.assertStoreAccess(request.storeId());
        requireStore(request.storeId());
        requireArea(request.storeId(), request.areaName());

        TableEntity entity = requireTable(id);
        permissionService.assertStoreAccess(entity.getStoreId());
        applyTableValues(entity, request);
        tableMapper.updateById(entity);
        return getSummary(id);
    }

    public TableSummary updateStatus(Long id, TableStatusUpdateRequest request) {
        TableEntity entity = requireTable(id);
        permissionService.assertStoreAccess(entity.getStoreId());
        entity.setStatus(normalizeStatus(request.status()));
        entity.setUpdater(permissionService.currentUser().username());
        tableMapper.updateById(entity);
        return getSummary(id);
    }

    private Map<Long, TableCurrentOrderSummary> buildCurrentOrderMap(List<Long> storeIds) {
        List<OrderEntity> openOrders = orderMapper.selectList(
                new LambdaQueryWrapper<OrderEntity>()
                        .in(OrderEntity::getStoreId, storeIds)
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

    private List<Long> resolveStoreScope(Long storeId) {
        if (storeId != null) {
            permissionService.assertStoreAccess(storeId);
            return List.of(storeId);
        }
        return permissionService.currentStoreIds();
    }

    private Map<Long, String> loadStoreNameMap(List<Long> storeIds) {
        return storeMapper.selectList(new LambdaQueryWrapper<StoreEntity>().in(StoreEntity::getId, storeIds))
                .stream()
                .collect(Collectors.toMap(StoreEntity::getId, StoreEntity::getName, (left, right) -> left, LinkedHashMap::new));
    }

    private void applyTableValues(TableEntity entity, TableUpsertRequest request) {
        String areaName = request.areaName().trim();
        String tableName = request.tableName().trim();
        validateTableUnique(entity.getId(), request.storeId(), tableName);
        entity.setStoreId(request.storeId());
        entity.setAreaName(areaName);
        entity.setTableName(tableName);
        entity.setCapacity(request.capacity());
        entity.setStatus(normalizeStatus(request.status()));
        entity.setUpdater(permissionService.currentUser().username());
    }

    private void validateTableUnique(Long currentId, Long storeId, String tableName) {
        List<TableEntity> duplicates = tableMapper.selectList(
                new LambdaQueryWrapper<TableEntity>()
                        .eq(TableEntity::getStoreId, storeId)
                        .eq(TableEntity::getTableName, tableName));
        boolean exists = duplicates.stream().anyMatch(item -> !item.getId().equals(currentId));
        if (exists) {
            throw new BadRequestException("Table name already exists in this store");
        }
    }

    private void requireStore(Long storeId) {
        if (storeMapper.selectById(storeId) == null) {
            throw new NotFoundException("Store not found");
        }
    }

    private void requireArea(Long storeId, String areaName) {
        boolean exists = tableAreaMapper.selectCount(
                new LambdaQueryWrapper<TableAreaEntity>()
                        .eq(TableAreaEntity::getStoreId, storeId)
                        .eq(TableAreaEntity::getAreaName, areaName.trim())
                        .eq(TableAreaEntity::getEnabled, true)) > 0;
        if (exists) {
            return;
        }
        throw new BadRequestException("Table area does not exist");
    }

    private TableEntity requireTable(Long id) {
        TableEntity entity = tableMapper.selectById(id);
        if (entity == null) {
            throw new NotFoundException("Table not found");
        }
        return entity;
    }

    private String normalizeStatus(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!TABLE_STATUSES.contains(normalized)) {
            throw new BadRequestException("Unsupported table status");
        }
        return normalized;
    }

    private TableSummary getSummary(Long id) {
        TableEntity entity = requireTable(id);
        Map<Long, String> storeNameMap = loadStoreNameMap(List.of(entity.getStoreId()));
        Map<Long, TableCurrentOrderSummary> currentOrderMap = buildCurrentOrderMap(List.of(entity.getStoreId()));
        return new TableSummary(
                entity.getId(),
                entity.getStoreId(),
                storeNameMap.getOrDefault(entity.getStoreId(), ""),
                entity.getAreaName(),
                entity.getTableName(),
                entity.getTableName(),
                entity.getCapacity() == null ? 0 : entity.getCapacity(),
                entity.getStatus(),
                currentOrderMap.get(entity.getId()));
    }
}
