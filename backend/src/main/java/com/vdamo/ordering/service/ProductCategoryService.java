package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.vdamo.ordering.common.exception.BadRequestException;
import com.vdamo.ordering.common.exception.NotFoundException;
import com.vdamo.ordering.common.support.IdGenerator;
import com.vdamo.ordering.entity.ProductCategoryEntity;
import com.vdamo.ordering.entity.ProductEntity;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.mapper.ProductCategoryMapper;
import com.vdamo.ordering.mapper.ProductMapper;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.model.ProductCategoryEnabledUpdateRequest;
import com.vdamo.ordering.model.ProductCategorySummary;
import com.vdamo.ordering.model.ProductCategoryUpsertRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProductCategoryService {

    private final ProductCategoryMapper productCategoryMapper;
    private final ProductMapper productMapper;
    private final StoreMapper storeMapper;
    private final PermissionService permissionService;
    private final IdGenerator idGenerator;

    public ProductCategoryService(
            ProductCategoryMapper productCategoryMapper,
            ProductMapper productMapper,
            StoreMapper storeMapper,
            PermissionService permissionService,
            IdGenerator idGenerator
    ) {
        this.productCategoryMapper = productCategoryMapper;
        this.productMapper = productMapper;
        this.storeMapper = storeMapper;
        this.permissionService = permissionService;
        this.idGenerator = idGenerator;
    }

    public List<ProductCategorySummary> list(Long storeId, String keyword, Boolean enabled) {
        List<Long> storeScope = permissionService.currentStoreIds();
        if (storeId != null) {
            permissionService.assertStoreAccess(storeId);
        }

        LambdaQueryWrapper<ProductCategoryEntity> wrapper = new LambdaQueryWrapper<ProductCategoryEntity>()
                .in(ProductCategoryEntity::getStoreId, storeScope)
                .orderByAsc(ProductCategoryEntity::getStoreId)
                .orderByAsc(ProductCategoryEntity::getSortOrder)
                .orderByAsc(ProductCategoryEntity::getId);
        if (storeId != null) {
            wrapper.eq(ProductCategoryEntity::getStoreId, storeId);
        }
        if (enabled != null) {
            wrapper.eq(ProductCategoryEntity::getEnabled, enabled);
        }
        if (StringUtils.hasText(keyword)) {
            String keywordValue = keyword.trim();
            wrapper.and(query -> query.like(ProductCategoryEntity::getCategoryName, keywordValue)
                    .or()
                    .like(ProductCategoryEntity::getCategoryCode, keywordValue));
        }

        List<ProductCategoryEntity> categories = productCategoryMapper.selectList(wrapper);
        if (categories.isEmpty()) {
            return List.of();
        }

        Map<Long, String> storeNameMap = loadStoreNameMap(storeScope);
        Map<Long, Map<String, Long>> productCountMap = loadProductCountMap(categories);

        return categories.stream()
                .map(category -> toSummary(category, storeNameMap, productCountMap))
                .toList();
    }

    public ProductCategorySummary create(ProductCategoryUpsertRequest request) {
        permissionService.assertStoreAccess(request.storeId());
        requireStore(request.storeId());

        ProductCategoryEntity entity = new ProductCategoryEntity();
        entity.setId(idGenerator.nextId());
        applyCategoryValues(entity, request);
        productCategoryMapper.insert(entity);
        return getSummary(entity.getId());
    }

    public ProductCategorySummary update(Long id, ProductCategoryUpsertRequest request) {
        permissionService.assertStoreAccess(request.storeId());
        requireStore(request.storeId());

        ProductCategoryEntity entity = requireCategory(id);
        permissionService.assertStoreAccess(entity.getStoreId());
        String previousCategoryCode = entity.getCategoryCode();
        Long previousStoreId = entity.getStoreId();

        applyCategoryValues(entity, request);
        productCategoryMapper.updateById(entity);

        if (!previousStoreId.equals(entity.getStoreId()) || !previousCategoryCode.equals(entity.getCategoryCode())) {
            productMapper.update(
                    null,
                    new LambdaUpdateWrapper<ProductEntity>()
                            .eq(ProductEntity::getStoreId, previousStoreId)
                            .eq(ProductEntity::getCategoryCode, previousCategoryCode)
                            .set(ProductEntity::getStoreId, entity.getStoreId())
                            .set(ProductEntity::getCategoryCode, entity.getCategoryCode()));
        }
        return getSummary(entity.getId());
    }

    public ProductCategorySummary updateEnabled(Long id, ProductCategoryEnabledUpdateRequest request) {
        ProductCategoryEntity entity = requireCategory(id);
        permissionService.assertStoreAccess(entity.getStoreId());
        entity.setEnabled(request.enabled());
        entity.setUpdater(permissionService.currentUser().username());
        productCategoryMapper.updateById(entity);
        return getSummary(id);
    }

    public void delete(Long id) {
        ProductCategoryEntity entity = requireCategory(id);
        permissionService.assertStoreAccess(entity.getStoreId());

        Long productCount = productMapper.selectCount(
                new LambdaQueryWrapper<ProductEntity>()
                        .eq(ProductEntity::getStoreId, entity.getStoreId())
                        .eq(ProductEntity::getCategoryCode, entity.getCategoryCode()));
        if (productCount != null && productCount > 0) {
            throw new BadRequestException("Product category has products and cannot be deleted");
        }

        productCategoryMapper.deleteById(entity.getId());
    }

    private void applyCategoryValues(ProductCategoryEntity entity, ProductCategoryUpsertRequest request) {
        String categoryName = request.categoryName().trim();
        String categoryCode = request.categoryCode().trim().toUpperCase(Locale.ROOT);

        validateCategoryUnique(entity.getId(), request.storeId(), categoryName, categoryCode);
        entity.setStoreId(request.storeId());
        entity.setCategoryName(categoryName);
        entity.setCategoryCode(categoryCode);
        entity.setSortOrder(request.sortOrder());
        entity.setEnabled(request.enabled());
        entity.setUpdater(permissionService.currentUser().username());
    }

    private void validateCategoryUnique(Long currentId, Long storeId, String categoryName, String categoryCode) {
        List<ProductCategoryEntity> duplicates = productCategoryMapper.selectList(
                new LambdaQueryWrapper<ProductCategoryEntity>()
                        .eq(ProductCategoryEntity::getStoreId, storeId));
        boolean duplicatedName = duplicates.stream()
                .anyMatch(item -> !item.getId().equals(currentId) && item.getCategoryName().equalsIgnoreCase(categoryName));
        if (duplicatedName) {
            throw new BadRequestException("Category name already exists in this store");
        }
        boolean duplicatedCode = duplicates.stream()
                .anyMatch(item -> !item.getId().equals(currentId) && item.getCategoryCode().equalsIgnoreCase(categoryCode));
        if (duplicatedCode) {
            throw new BadRequestException("Category code already exists in this store");
        }
    }

    private void requireStore(Long storeId) {
        if (storeMapper.selectById(storeId) == null) {
            throw new NotFoundException("Store not found");
        }
    }

    private ProductCategoryEntity requireCategory(Long id) {
        ProductCategoryEntity entity = productCategoryMapper.selectById(id);
        if (entity == null) {
            throw new NotFoundException("Product category not found");
        }
        return entity;
    }

    private ProductCategorySummary getSummary(Long id) {
        ProductCategoryEntity entity = requireCategory(id);
        Map<Long, String> storeNameMap = loadStoreNameMap(List.of(entity.getStoreId()));
        Map<Long, Map<String, Long>> productCountMap = loadProductCountMap(List.of(entity));
        return toSummary(entity, storeNameMap, productCountMap);
    }

    private Map<Long, String> loadStoreNameMap(List<Long> storeIds) {
        return storeMapper.selectList(
                        new LambdaQueryWrapper<StoreEntity>().in(StoreEntity::getId, storeIds))
                .stream()
                .collect(Collectors.toMap(StoreEntity::getId, StoreEntity::getName, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, Map<String, Long>> loadProductCountMap(List<ProductCategoryEntity> categories) {
        List<Long> categoryStoreIds = categories.stream()
                .map(ProductCategoryEntity::getStoreId)
                .distinct()
                .toList();
        return productMapper.selectList(
                        new LambdaQueryWrapper<ProductEntity>().in(ProductEntity::getStoreId, categoryStoreIds))
                .stream()
                .collect(Collectors.groupingBy(
                        ProductEntity::getStoreId,
                        LinkedHashMap::new,
                        Collectors.groupingBy(ProductEntity::getCategoryCode, LinkedHashMap::new, Collectors.counting())));
    }

    private ProductCategorySummary toSummary(
            ProductCategoryEntity category,
            Map<Long, String> storeNameMap,
            Map<Long, Map<String, Long>> productCountMap
    ) {
        return new ProductCategorySummary(
                category.getId(),
                category.getStoreId(),
                storeNameMap.getOrDefault(category.getStoreId(), ""),
                category.getCategoryName(),
                category.getCategoryCode(),
                defaultInt(category.getSortOrder()),
                Boolean.TRUE.equals(category.getEnabled()),
                resolveCount(productCountMap, category.getStoreId(), category.getCategoryCode()));
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private int resolveCount(Map<Long, Map<String, Long>> countMap, Long storeId, String key) {
        return countMap.getOrDefault(storeId, Map.of())
                .getOrDefault(key, 0L)
                .intValue();
    }
}
