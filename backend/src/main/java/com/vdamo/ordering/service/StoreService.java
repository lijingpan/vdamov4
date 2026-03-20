package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.common.exception.BadRequestException;
import com.vdamo.ordering.common.exception.NotFoundException;
import com.vdamo.ordering.common.support.IdGenerator;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.entity.SysUserStoreEntity;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.mapper.SysUserStoreMapper;
import com.vdamo.ordering.model.StoreStatusUpdateRequest;
import com.vdamo.ordering.model.StoreSummary;
import com.vdamo.ordering.model.StoreUpsertRequest;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StoreService {

    private static final List<String> STORE_STATUSES = List.of("OPEN", "REST", "DISABLED");

    private final StoreMapper storeMapper;
    private final SysUserStoreMapper sysUserStoreMapper;
    private final PermissionService permissionService;
    private final IdGenerator idGenerator;

    public StoreService(
            StoreMapper storeMapper,
            SysUserStoreMapper sysUserStoreMapper,
            PermissionService permissionService,
            IdGenerator idGenerator
    ) {
        this.storeMapper = storeMapper;
        this.sysUserStoreMapper = sysUserStoreMapper;
        this.permissionService = permissionService;
        this.idGenerator = idGenerator;
    }

    public List<StoreSummary> list(String keyword, String status) {
        List<Long> storeScope = permissionService.currentStoreIds();
        String normalizedStatus = StringUtils.hasText(status) ? normalizeStoreStatus(status) : null;

        LambdaQueryWrapper<StoreEntity> wrapper = new LambdaQueryWrapper<StoreEntity>()
                .in(StoreEntity::getId, storeScope)
                .orderByAsc(StoreEntity::getName)
                .orderByAsc(StoreEntity::getId);
        if (normalizedStatus != null) {
            wrapper.eq(StoreEntity::getStatus, normalizedStatus);
        }
        if (StringUtils.hasText(keyword)) {
            String keywordValue = keyword.trim();
            wrapper.and(query -> query.like(StoreEntity::getName, keywordValue)
                    .or()
                    .like(StoreEntity::getCountryCode, keywordValue)
                    .or()
                    .like(StoreEntity::getId, keywordValue));
        }

        return storeMapper.selectList(
                        wrapper)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    public StoreSummary create(StoreUpsertRequest request) {
        permissionService.assertSuperAdmin();

        StoreEntity entity = new StoreEntity();
        entity.setId(idGenerator.nextId());
        applyStoreValues(entity, request);
        storeMapper.insert(entity);
        bindCurrentUserToStore(entity.getId());
        return toSummary(entity);
    }

    public StoreSummary update(Long id, StoreUpsertRequest request) {
        permissionService.assertStoreAccess(id);

        StoreEntity entity = requireStore(id);
        applyStoreValues(entity, request);
        storeMapper.updateById(entity);
        return toSummary(entity);
    }

    public StoreSummary updateStatus(Long id, StoreStatusUpdateRequest request) {
        permissionService.assertStoreAccess(id);

        StoreEntity entity = requireStore(id);
        String status = normalizeStoreStatus(request.businessStatus());
        entity.setStatus(status);
        entity.setUpdater(permissionService.currentUser().username());
        storeMapper.updateById(entity);
        return toSummary(entity);
    }

    private void applyStoreValues(StoreEntity entity, StoreUpsertRequest request) {
        String name = request.name().trim();
        String countryCode = request.countryCode().trim().toUpperCase(Locale.ROOT);
        String status = normalizeStoreStatus(request.businessStatus());
        List<String> businessTypes = request.businessTypes().stream()
                .filter(StringUtils::hasText)
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .distinct()
                .toList();

        if (businessTypes.isEmpty()) {
            throw new BadRequestException("At least one business type is required");
        }

        validateStoreNameUnique(entity.getId(), name);
        entity.setName(name);
        entity.setCountryCode(countryCode);
        entity.setStatus(status);
        entity.setBusinessModes(String.join(",", businessTypes));
        entity.setUpdater(permissionService.currentUser().username());
    }

    private void validateStoreNameUnique(Long currentId, String name) {
        List<StoreEntity> duplicates = storeMapper.selectList(
                new LambdaQueryWrapper<StoreEntity>()
                        .eq(StoreEntity::getName, name));
        boolean exists = duplicates.stream().anyMatch(item -> !item.getId().equals(currentId));
        if (exists) {
            throw new BadRequestException("Store name already exists");
        }
    }

    private StoreEntity requireStore(Long id) {
        StoreEntity entity = storeMapper.selectById(id);
        if (entity == null) {
            throw new NotFoundException("Store not found");
        }
        return entity;
    }

    private void bindCurrentUserToStore(Long storeId) {
        Long userId = permissionService.currentUser().userId();
        boolean alreadyLinked = sysUserStoreMapper.selectCount(
                new LambdaQueryWrapper<SysUserStoreEntity>()
                        .eq(SysUserStoreEntity::getUserId, userId)
                        .eq(SysUserStoreEntity::getStoreId, storeId)) > 0;
        if (alreadyLinked) {
            return;
        }

        SysUserStoreEntity link = new SysUserStoreEntity();
        link.setId(idGenerator.nextId());
        link.setUserId(userId);
        link.setStoreId(storeId);
        link.setUpdater(permissionService.currentUser().username());
        sysUserStoreMapper.insert(link);
    }

    private String normalizeStoreStatus(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!STORE_STATUSES.contains(normalized)) {
            throw new BadRequestException("Unsupported store status");
        }
        return normalized;
    }

    private StoreSummary toSummary(StoreEntity entity) {
        List<String> businessTypes = StringUtils.hasText(entity.getBusinessModes())
                ? List.of(entity.getBusinessModes().split(","))
                : List.of();
        return new StoreSummary(
                entity.getId(),
                entity.getName(),
                entity.getCountryCode(),
                entity.getStatus(),
                businessTypes);
    }
}
