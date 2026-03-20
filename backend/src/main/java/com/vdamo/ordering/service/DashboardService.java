package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.entity.MemberEntity;
import com.vdamo.ordering.entity.OrderEntity;
import com.vdamo.ordering.entity.ProductEntity;
import com.vdamo.ordering.entity.StoreDeviceEntity;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.entity.TableEntity;
import com.vdamo.ordering.mapper.MemberMapper;
import com.vdamo.ordering.mapper.OrderMapper;
import com.vdamo.ordering.mapper.ProductMapper;
import com.vdamo.ordering.mapper.StoreDeviceMapper;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.mapper.TableMapper;
import com.vdamo.ordering.model.DashboardSummaryResponse;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private static final Set<String> OPEN_ORDER_STATUS = Set.of(
            "PENDING",
            "CONFIRMED",
            "IN_PROGRESS",
            "COOKING",
            "SERVED",
            "WAITING_CHECKOUT"
    );

    private final StoreMapper storeMapper;
    private final ProductMapper productMapper;
    private final MemberMapper memberMapper;
    private final StoreDeviceMapper storeDeviceMapper;
    private final TableMapper tableMapper;
    private final OrderMapper orderMapper;
    private final PermissionService permissionService;

    public DashboardService(
            StoreMapper storeMapper,
            ProductMapper productMapper,
            MemberMapper memberMapper,
            StoreDeviceMapper storeDeviceMapper,
            TableMapper tableMapper,
            OrderMapper orderMapper,
            PermissionService permissionService
    ) {
        this.storeMapper = storeMapper;
        this.productMapper = productMapper;
        this.memberMapper = memberMapper;
        this.storeDeviceMapper = storeDeviceMapper;
        this.tableMapper = tableMapper;
        this.orderMapper = orderMapper;
        this.permissionService = permissionService;
    }

    public DashboardSummaryResponse getSummary() {
        var storeScope = permissionService.currentStoreIds();
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();

        int totalStores = Math.toIntExact(storeMapper.selectCount(
                new LambdaQueryWrapper<StoreEntity>().in(StoreEntity::getId, storeScope)));
        int activeProducts = Math.toIntExact(productMapper.selectCount(
                new LambdaQueryWrapper<ProductEntity>()
                        .in(ProductEntity::getStoreId, storeScope)
                        .eq(ProductEntity::getActive, true)));
        int totalMembers = Math.toIntExact(memberMapper.selectCount(
                new LambdaQueryWrapper<MemberEntity>().in(MemberEntity::getStoreId, storeScope)));
        int totalDevices = Math.toIntExact(storeDeviceMapper.selectCount(
                new LambdaQueryWrapper<StoreDeviceEntity>().in(StoreDeviceEntity::getStoreId, storeScope)));
        int totalTables = Math.toIntExact(tableMapper.selectCount(
                new LambdaQueryWrapper<TableEntity>().in(TableEntity::getStoreId, storeScope)));
        int activeOrders = Math.toIntExact(orderMapper.selectCount(
                new LambdaQueryWrapper<OrderEntity>()
                        .in(OrderEntity::getStoreId, storeScope)
                        .in(OrderEntity::getOrderStatus, OPEN_ORDER_STATUS)));
        int todayOrders = Math.toIntExact(orderMapper.selectCount(
                new LambdaQueryWrapper<OrderEntity>()
                        .in(OrderEntity::getStoreId, storeScope)
                        .ge(OrderEntity::getCreateTime, todayStart)));

        int availableTables = Math.toIntExact(tableMapper.selectCount(
                new LambdaQueryWrapper<TableEntity>()
                        .in(TableEntity::getStoreId, storeScope)
                        .eq(TableEntity::getStatus, "IDLE")));
        int occupiedTables = Math.toIntExact(tableMapper.selectCount(
                new LambdaQueryWrapper<TableEntity>()
                        .in(TableEntity::getStoreId, storeScope)
                        .eq(TableEntity::getStatus, "IN_USE")));
        int checkoutTables = Math.toIntExact(tableMapper.selectCount(
                new LambdaQueryWrapper<TableEntity>()
                        .in(TableEntity::getStoreId, storeScope)
                        .eq(TableEntity::getStatus, "WAITING_CHECKOUT")));
        int disabledTables = Math.toIntExact(tableMapper.selectCount(
                new LambdaQueryWrapper<TableEntity>()
                        .in(TableEntity::getStoreId, storeScope)
                        .eq(TableEntity::getStatus, "DISABLED")));

        return new DashboardSummaryResponse(
                totalStores,
                activeProducts,
                totalMembers,
                totalDevices,
                totalTables,
                activeOrders,
                todayOrders,
                Map.of(
                        "available", availableTables,
                        "occupied", occupiedTables,
                        "checkout", checkoutTables,
                        "disabled", disabledTables)
        );
    }
}
