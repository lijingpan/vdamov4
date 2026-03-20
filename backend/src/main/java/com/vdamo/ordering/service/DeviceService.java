package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.entity.StoreDeviceEntity;
import com.vdamo.ordering.mapper.StoreDeviceMapper;
import com.vdamo.ordering.model.DeviceSummary;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {

    private final StoreDeviceMapper storeDeviceMapper;
    private final PermissionService permissionService;

    public DeviceService(StoreDeviceMapper storeDeviceMapper, PermissionService permissionService) {
        this.storeDeviceMapper = storeDeviceMapper;
        this.permissionService = permissionService;
    }

    public List<DeviceSummary> listByStore(Long storeId) {
        permissionService.assertStoreAccess(storeId);
        return storeDeviceMapper.selectList(
                        new LambdaQueryWrapper<StoreDeviceEntity>().eq(StoreDeviceEntity::getStoreId, storeId))
                .stream()
                .map(entity -> new DeviceSummary(
                        entity.getId(),
                        entity.getStoreId(),
                        entity.getName(),
                        entity.getType(),
                        entity.getBrand(),
                        entity.getSn(),
                        entity.getSize(),
                        Boolean.TRUE.equals(entity.getEnabled())))
                .toList();
    }
}
