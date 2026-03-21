package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.common.exception.BadRequestException;
import com.vdamo.ordering.common.exception.NotFoundException;
import com.vdamo.ordering.common.support.IdGenerator;
import com.vdamo.ordering.entity.ProductCategoryEntity;
import com.vdamo.ordering.entity.ProductEntity;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.mapper.ProductCategoryMapper;
import com.vdamo.ordering.mapper.ProductMapper;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.model.ProductStatusUpdateRequest;
import com.vdamo.ordering.model.ProductSummary;
import com.vdamo.ordering.model.ProductUpsertRequest;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final StoreMapper storeMapper;
    private final PermissionService permissionService;
    private final IdGenerator idGenerator;

    public ProductService(
            ProductMapper productMapper,
            ProductCategoryMapper productCategoryMapper,
            StoreMapper storeMapper,
            PermissionService permissionService,
            IdGenerator idGenerator
    ) {
        this.productMapper = productMapper;
        this.productCategoryMapper = productCategoryMapper;
        this.storeMapper = storeMapper;
        this.permissionService = permissionService;
        this.idGenerator = idGenerator;
    }

    public List<ProductSummary> list(Long storeId, Boolean active, String categoryCode, String keyword) {
        List<Long> storeScope = permissionService.currentStoreIds();
        if (storeId != null) {
            permissionService.assertStoreAccess(storeId);
        }

        LambdaQueryWrapper<ProductEntity> wrapper = new LambdaQueryWrapper<ProductEntity>()
                .in(ProductEntity::getStoreId, storeScope)
                .orderByAsc(ProductEntity::getStoreId)
                .orderByAsc(ProductEntity::getCategoryCode)
                .orderByAsc(ProductEntity::getName);
        if (storeId != null) {
            wrapper.eq(ProductEntity::getStoreId, storeId);
        }
        if (active != null) {
            wrapper.eq(ProductEntity::getActive, active);
        }
        if (StringUtils.hasText(categoryCode)) {
            wrapper.eq(ProductEntity::getCategoryCode, categoryCode.trim().toUpperCase(Locale.ROOT));
        }
        if (StringUtils.hasText(keyword)) {
            String keywordValue = keyword.trim();
            wrapper.and(q -> q.like(ProductEntity::getName, keywordValue)
                    .or()
                    .like(ProductEntity::getCode, keywordValue)
                    .or()
                    .like(ProductEntity::getCategoryCode, keywordValue));
        }

        Map<Long, String> storeNameMap = loadStoreNameMap(storeScope);
        return productMapper.selectList(wrapper)
                .stream()
                .map(entity -> toSummary(entity, storeNameMap))
                .toList();
    }

    public ProductSummary create(ProductUpsertRequest request) {
        permissionService.assertStoreAccess(request.storeId());
        requireStore(request.storeId());

        ProductEntity entity = new ProductEntity();
        entity.setId(idGenerator.nextId());
        applyProductValues(entity, request);
        productMapper.insert(entity);
        return getSummary(entity.getId());
    }

    public ProductSummary update(Long id, ProductUpsertRequest request) {
        permissionService.assertStoreAccess(request.storeId());
        requireStore(request.storeId());

        ProductEntity entity = requireProduct(id);
        permissionService.assertStoreAccess(entity.getStoreId());
        applyProductValues(entity, request);
        productMapper.updateById(entity);
        return getSummary(entity.getId());
    }

    public ProductSummary updateStatus(Long id, ProductStatusUpdateRequest request) {
        ProductEntity entity = requireProduct(id);
        permissionService.assertStoreAccess(entity.getStoreId());
        entity.setActive(request.active());
        entity.setUpdater(permissionService.currentUser().username());
        productMapper.updateById(entity);
        return getSummary(id);
    }

    private void applyProductValues(ProductEntity entity, ProductUpsertRequest request) {
        String name = request.name().trim();
        String code = request.code().trim();
        String categoryCode = request.categoryCode().trim().toUpperCase(Locale.ROOT);

        validateProductUnique(entity.getId(), request.storeId(), code);
        requireCategory(request.storeId(), categoryCode);

        entity.setStoreId(request.storeId());
        entity.setName(name);
        entity.setCode(code);
        entity.setCategoryCode(categoryCode);
        entity.setPriceInCent(request.priceInCent());
        entity.setActive(request.active());
        entity.setUpdater(permissionService.currentUser().username());
    }

    private void validateProductUnique(Long currentId, Long storeId, String code) {
        List<ProductEntity> duplicates = productMapper.selectList(
                new LambdaQueryWrapper<ProductEntity>()
                        .eq(ProductEntity::getStoreId, storeId)
                        .eq(ProductEntity::getCode, code));
        boolean exists = duplicates.stream().anyMatch(item -> !item.getId().equals(currentId));
        if (exists) {
            throw new BadRequestException("Product code already exists in this store");
        }
    }

    private void requireCategory(Long storeId, String categoryCode) {
        boolean exists = productCategoryMapper.selectCount(
                new LambdaQueryWrapper<ProductCategoryEntity>()
                        .eq(ProductCategoryEntity::getStoreId, storeId)
                        .eq(ProductCategoryEntity::getCategoryCode, categoryCode)) > 0;
        if (!exists) {
            throw new BadRequestException("Product category does not exist");
        }
    }

    private void requireStore(Long storeId) {
        if (storeMapper.selectById(storeId) == null) {
            throw new NotFoundException("Store not found");
        }
    }

    private ProductEntity requireProduct(Long id) {
        ProductEntity entity = productMapper.selectById(id);
        if (entity == null) {
            throw new NotFoundException("Product not found");
        }
        return entity;
    }

    private ProductSummary getSummary(Long id) {
        ProductEntity entity = requireProduct(id);
        Map<Long, String> storeNameMap = loadStoreNameMap(List.of(entity.getStoreId()));
        return toSummary(entity, storeNameMap);
    }

    private Map<Long, String> loadStoreNameMap(List<Long> storeIds) {
        return storeMapper.selectList(
                        new LambdaQueryWrapper<StoreEntity>().in(StoreEntity::getId, storeIds))
                .stream()
                .collect(Collectors.toMap(StoreEntity::getId, StoreEntity::getName, (left, right) -> left));
    }

    private ProductSummary toSummary(ProductEntity entity, Map<Long, String> storeNameMap) {
        return new ProductSummary(
                entity.getId(),
                entity.getStoreId(),
                storeNameMap.getOrDefault(entity.getStoreId(), ""),
                entity.getName(),
                entity.getCode(),
                entity.getCategoryCode(),
                entity.getPriceInCent() == null ? 0 : entity.getPriceInCent(),
                Boolean.TRUE.equals(entity.getActive()));
    }
}
