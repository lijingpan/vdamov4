package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.entity.StoreDeviceEntity;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.mapper.StoreDeviceMapper;
import com.vdamo.ordering.model.DeviceSummary;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DeviceService {

    private final StoreDeviceMapper storeDeviceMapper;
    private final StoreMapper storeMapper;
    private final PermissionService permissionService;

    public DeviceService(
            StoreDeviceMapper storeDeviceMapper,
            StoreMapper storeMapper,
            PermissionService permissionService
    ) {
        this.storeDeviceMapper = storeDeviceMapper;
        this.storeMapper = storeMapper;
        this.permissionService = permissionService;
    }

    public List<DeviceSummary> list(Long storeId, String type, Boolean enabled, String keyword) {
        List<Long> storeScope = permissionService.currentStoreIds();
        if (storeId != null) {
            permissionService.assertStoreAccess(storeId);
        }

        LambdaQueryWrapper<StoreDeviceEntity> wrapper = new LambdaQueryWrapper<StoreDeviceEntity>()
                .in(StoreDeviceEntity::getStoreId, storeScope)
                .orderByAsc(StoreDeviceEntity::getStoreId)
                .orderByAsc(StoreDeviceEntity::getType)
                .orderByAsc(StoreDeviceEntity::getName);
        if (storeId != null) {
            wrapper.eq(StoreDeviceEntity::getStoreId, storeId);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(StoreDeviceEntity::getType, type.trim());
        }
        if (enabled != null) {
            wrapper.eq(StoreDeviceEntity::getEnabled, enabled);
        }
        if (StringUtils.hasText(keyword)) {
            String keywordValue = keyword.trim();
            wrapper.and(q -> q.like(StoreDeviceEntity::getName, keywordValue)
                    .or()
                    .like(StoreDeviceEntity::getSn, keywordValue)
                    .or()
                    .like(StoreDeviceEntity::getBrand, keywordValue)
                    .or()
                    .like(StoreDeviceEntity::getPurpose, keywordValue));
        }

        Map<Long, String> storeNameMap = storeMapper.selectList(
                        new LambdaQueryWrapper<StoreEntity>().in(StoreEntity::getId, storeScope))
                .stream()
                .collect(Collectors.toMap(StoreEntity::getId, StoreEntity::getName, (left, right) -> left));

        return storeDeviceMapper.selectList(wrapper)
                .stream()
                .map(entity -> new DeviceSummary(
                        entity.getId(),
                        entity.getStoreId(),
                        storeNameMap.getOrDefault(entity.getStoreId(), ""),
                        entity.getName(),
                        entity.getType(),
                        entity.getPurpose(),
                        entity.getBrand(),
                        entity.getSn(),
                        entity.getSize(),
                        entity.getOnlineStatus(),
                        Boolean.TRUE.equals(entity.getEnabled())))
                .toList();
    }
}
