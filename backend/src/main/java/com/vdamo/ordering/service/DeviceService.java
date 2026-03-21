package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.common.exception.BadRequestException;
import com.vdamo.ordering.common.exception.NotFoundException;
import com.vdamo.ordering.common.support.IdGenerator;
import com.vdamo.ordering.entity.StoreDeviceEntity;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.mapper.StoreDeviceMapper;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.model.DeviceEnabledUpdateRequest;
import com.vdamo.ordering.model.DeviceSummary;
import com.vdamo.ordering.model.DeviceUpsertRequest;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DeviceService {

    private final StoreDeviceMapper storeDeviceMapper;
    private final StoreMapper storeMapper;
    private final PermissionService permissionService;
    private final IdGenerator idGenerator;

    public DeviceService(
            StoreDeviceMapper storeDeviceMapper,
            StoreMapper storeMapper,
            PermissionService permissionService,
            IdGenerator idGenerator
    ) {
        this.storeDeviceMapper = storeDeviceMapper;
        this.storeMapper = storeMapper;
        this.permissionService = permissionService;
        this.idGenerator = idGenerator;
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
            wrapper.eq(StoreDeviceEntity::getType, type.trim().toUpperCase(Locale.ROOT));
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

        Map<Long, String> storeNameMap = loadStoreNameMap(storeScope);
        return storeDeviceMapper.selectList(wrapper)
                .stream()
                .map(entity -> toSummary(entity, storeNameMap))
                .toList();
    }

    public DeviceSummary create(DeviceUpsertRequest request) {
        permissionService.assertStoreAccess(request.storeId());
        requireStore(request.storeId());

        StoreDeviceEntity entity = new StoreDeviceEntity();
        entity.setId(idGenerator.nextId());
        applyDeviceValues(entity, request);
        storeDeviceMapper.insert(entity);
        return getSummary(entity.getId());
    }

    public DeviceSummary update(Long id, DeviceUpsertRequest request) {
        permissionService.assertStoreAccess(request.storeId());
        requireStore(request.storeId());

        StoreDeviceEntity entity = requireDevice(id);
        permissionService.assertStoreAccess(entity.getStoreId());
        applyDeviceValues(entity, request);
        storeDeviceMapper.updateById(entity);
        return getSummary(id);
    }

    public DeviceSummary updateEnabled(Long id, DeviceEnabledUpdateRequest request) {
        StoreDeviceEntity entity = requireDevice(id);
        permissionService.assertStoreAccess(entity.getStoreId());
        entity.setEnabled(request.enabled());
        entity.setUpdater(permissionService.currentUser().username());
        storeDeviceMapper.updateById(entity);
        return getSummary(id);
    }

    private void applyDeviceValues(StoreDeviceEntity entity, DeviceUpsertRequest request) {
        String name = request.name().trim();
        String type = request.type().trim().toUpperCase(Locale.ROOT);
        String purpose = normalizeOptionalUpper(request.purpose());
        String brand = normalizeOptional(request.brand());
        String sn = normalizeOptional(request.sn());
        String size = normalizeOptional(request.size());
        String onlineStatus = normalizeOptionalUpper(request.onlineStatus());

        validateDeviceSnUnique(entity.getId(), request.storeId(), sn);

        entity.setStoreId(request.storeId());
        entity.setName(name);
        entity.setType(type);
        entity.setPurpose(purpose);
        entity.setBrand(brand);
        entity.setSn(sn);
        entity.setSize(size);
        entity.setOnlineStatus(onlineStatus);
        entity.setEnabled(Boolean.TRUE.equals(request.enabled()));
        entity.setUpdater(permissionService.currentUser().username());
    }

    private void validateDeviceSnUnique(Long currentId, Long storeId, String sn) {
        if (!StringUtils.hasText(sn)) {
            return;
        }
        List<StoreDeviceEntity> duplicates = storeDeviceMapper.selectList(
                new LambdaQueryWrapper<StoreDeviceEntity>()
                        .eq(StoreDeviceEntity::getStoreId, storeId)
                        .eq(StoreDeviceEntity::getSn, sn));
        boolean exists = duplicates.stream().anyMatch(item -> !item.getId().equals(currentId));
        if (exists) {
            throw new BadRequestException("Device SN already exists in this store");
        }
    }

    private void requireStore(Long storeId) {
        if (storeMapper.selectById(storeId) == null) {
            throw new NotFoundException("Store not found");
        }
    }

    private StoreDeviceEntity requireDevice(Long id) {
        StoreDeviceEntity entity = storeDeviceMapper.selectById(id);
        if (entity == null) {
            throw new NotFoundException("Device not found");
        }
        return entity;
    }

    private DeviceSummary getSummary(Long id) {
        StoreDeviceEntity entity = requireDevice(id);
        Map<Long, String> storeNameMap = loadStoreNameMap(List.of(entity.getStoreId()));
        return toSummary(entity, storeNameMap);
    }

    private Map<Long, String> loadStoreNameMap(List<Long> storeIds) {
        return storeMapper.selectList(
                        new LambdaQueryWrapper<StoreEntity>().in(StoreEntity::getId, storeIds))
                .stream()
                .collect(Collectors.toMap(StoreEntity::getId, StoreEntity::getName, (left, right) -> left));
    }

    private DeviceSummary toSummary(StoreDeviceEntity entity, Map<Long, String> storeNameMap) {
        return new DeviceSummary(
                entity.getId(),
                entity.getStoreId(),
                storeNameMap.getOrDefault(entity.getStoreId(), ""),
                normalizeOptional(entity.getName()),
                normalizeOptional(entity.getType()),
                normalizeOptional(entity.getPurpose()),
                normalizeOptional(entity.getBrand()),
                normalizeOptional(entity.getSn()),
                normalizeOptional(entity.getSize()),
                normalizeOptional(entity.getOnlineStatus()),
                Boolean.TRUE.equals(entity.getEnabled()));
    }

    private String normalizeOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim();
    }

    private String normalizeOptionalUpper(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
