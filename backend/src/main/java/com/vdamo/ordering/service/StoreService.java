package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.common.exception.BadRequestException;
import com.vdamo.ordering.common.exception.NotFoundException;
import com.vdamo.ordering.common.support.IdGenerator;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.entity.SysUserStoreEntity;
import com.vdamo.ordering.entity.MemberEntity;
import com.vdamo.ordering.entity.OrderEntity;
import com.vdamo.ordering.entity.ProductCategoryEntity;
import com.vdamo.ordering.entity.ProductEntity;
import com.vdamo.ordering.entity.StoreDeviceEntity;
import com.vdamo.ordering.entity.TableAreaEntity;
import com.vdamo.ordering.entity.TableEntity;
import com.vdamo.ordering.mapper.MemberMapper;
import com.vdamo.ordering.mapper.OrderMapper;
import com.vdamo.ordering.mapper.ProductCategoryMapper;
import com.vdamo.ordering.mapper.ProductMapper;
import com.vdamo.ordering.mapper.StoreDeviceMapper;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.mapper.SysUserStoreMapper;
import com.vdamo.ordering.mapper.TableAreaMapper;
import com.vdamo.ordering.mapper.TableMapper;
import com.vdamo.ordering.model.StoreStatusUpdateRequest;
import com.vdamo.ordering.model.StoreSummary;
import com.vdamo.ordering.model.StoreUpsertRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StoreService {

    private static final List<String> STORE_STATUSES = List.of("OPEN", "REST", "DISABLED");
    private static final BigDecimal LATITUDE_MIN = new BigDecimal("-90");
    private static final BigDecimal LATITUDE_MAX = new BigDecimal("90");
    private static final BigDecimal LONGITUDE_MIN = new BigDecimal("-180");
    private static final BigDecimal LONGITUDE_MAX = new BigDecimal("180");

    private final StoreMapper storeMapper;
    private final SysUserStoreMapper sysUserStoreMapper;
    private final StoreDeviceMapper storeDeviceMapper;
    private final MemberMapper memberMapper;
    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final TableMapper tableMapper;
    private final TableAreaMapper tableAreaMapper;
    private final OrderMapper orderMapper;
    private final PermissionService permissionService;
    private final IdGenerator idGenerator;

    public StoreService(
            StoreMapper storeMapper,
            SysUserStoreMapper sysUserStoreMapper,
            StoreDeviceMapper storeDeviceMapper,
            MemberMapper memberMapper,
            ProductMapper productMapper,
            ProductCategoryMapper productCategoryMapper,
            TableMapper tableMapper,
            TableAreaMapper tableAreaMapper,
            OrderMapper orderMapper,
            PermissionService permissionService,
            IdGenerator idGenerator
    ) {
        this.storeMapper = storeMapper;
        this.sysUserStoreMapper = sysUserStoreMapper;
        this.storeDeviceMapper = storeDeviceMapper;
        this.memberMapper = memberMapper;
        this.productMapper = productMapper;
        this.productCategoryMapper = productCategoryMapper;
        this.tableMapper = tableMapper;
        this.tableAreaMapper = tableAreaMapper;
        this.orderMapper = orderMapper;
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
                    .like(StoreEntity::getAddress, keywordValue)
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

    public void delete(Long id) {
        permissionService.assertSuperAdmin();

        StoreEntity entity = requireStore(id);

        ensureNoDependencies(id);
        sysUserStoreMapper.delete(new LambdaQueryWrapper<SysUserStoreEntity>().eq(SysUserStoreEntity::getStoreId, id));
        storeMapper.deleteById(entity.getId());
    }

    private void ensureNoDependencies(Long storeId) {
        assertNoBindings(
                sysUserStoreMapper.selectCount(new LambdaQueryWrapper<SysUserStoreEntity>().eq(SysUserStoreEntity::getStoreId, storeId)),
                "Store is assigned to users and cannot be deleted");
        assertNoBindings(
                storeDeviceMapper.selectCount(new LambdaQueryWrapper<StoreDeviceEntity>().eq(StoreDeviceEntity::getStoreId, storeId)),
                "Store has devices and cannot be deleted");
        assertNoBindings(
                memberMapper.selectCount(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getStoreId, storeId)),
                "Store has members and cannot be deleted");
        assertNoBindings(
                productMapper.selectCount(new LambdaQueryWrapper<ProductEntity>().eq(ProductEntity::getStoreId, storeId)),
                "Store has products and cannot be deleted");
        assertNoBindings(
                productCategoryMapper.selectCount(new LambdaQueryWrapper<ProductCategoryEntity>().eq(ProductCategoryEntity::getStoreId, storeId)),
                "Store has product categories and cannot be deleted");
        assertNoBindings(
                tableMapper.selectCount(new LambdaQueryWrapper<TableEntity>().eq(TableEntity::getStoreId, storeId)),
                "Store has tables and cannot be deleted");
        assertNoBindings(
                tableAreaMapper.selectCount(new LambdaQueryWrapper<TableAreaEntity>().eq(TableAreaEntity::getStoreId, storeId)),
                "Store has table areas and cannot be deleted");
        assertNoBindings(
                orderMapper.selectCount(new LambdaQueryWrapper<OrderEntity>().eq(OrderEntity::getStoreId, storeId)),
                "Store has orders and cannot be deleted");
    }

    private void assertNoBindings(Long count, String message) {
        if (count != null && count > 0) {
            throw new BadRequestException(message);
        }
    }

    private void applyStoreValues(StoreEntity entity, StoreUpsertRequest request) {
        String name = request.name().trim();
        String countryCode = request.countryCode().trim().toUpperCase(Locale.ROOT);
        String address = normalizeRequired(request.address(), "Store address is required");
        BigDecimal latitude = request.latitude();
        BigDecimal longitude = request.longitude();
        String status = normalizeStoreStatus(request.businessStatus());
        List<String> businessTypes = request.businessTypes().stream()
                .filter(StringUtils::hasText)
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .distinct()
                .toList();
        validateCoordinateRange(latitude, LATITUDE_MIN, LATITUDE_MAX, "latitude");
        validateCoordinateRange(longitude, LONGITUDE_MIN, LONGITUDE_MAX, "longitude");

        if (businessTypes.isEmpty()) {
            throw new BadRequestException("At least one business type is required");
        }

        validateStoreNameUnique(entity.getId(), name);
        entity.setName(name);
        entity.setCountryCode(countryCode);
        entity.setAddress(address);
        entity.setLatitude(latitude);
        entity.setLongitude(longitude);
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

    private void validateCoordinateRange(BigDecimal value, BigDecimal min, BigDecimal max, String fieldName) {
        if (value == null) {
            throw new BadRequestException("Store " + fieldName + " is required");
        }
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new BadRequestException("Unsupported " + fieldName + " value");
        }
    }

    private String normalizeRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(message);
        }
        return value.trim();
    }

    private StoreSummary toSummary(StoreEntity entity) {
        List<String> businessTypes = StringUtils.hasText(entity.getBusinessModes())
                ? List.of(entity.getBusinessModes().split(","))
                : List.of();
        return new StoreSummary(
                entity.getId(),
                entity.getName(),
                entity.getCountryCode(),
                entity.getAddress(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getStatus(),
                businessTypes);
    }
}
