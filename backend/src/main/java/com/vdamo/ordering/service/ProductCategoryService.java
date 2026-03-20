package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.entity.ProductCategoryEntity;
import com.vdamo.ordering.entity.ProductEntity;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.mapper.ProductCategoryMapper;
import com.vdamo.ordering.mapper.ProductMapper;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.model.ProductCategorySummary;
import java.util.LinkedHashMap;
import java.util.List;
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

    public ProductCategoryService(
            ProductCategoryMapper productCategoryMapper,
            ProductMapper productMapper,
            StoreMapper storeMapper,
            PermissionService permissionService
    ) {
        this.productCategoryMapper = productCategoryMapper;
        this.productMapper = productMapper;
        this.storeMapper = storeMapper;
        this.permissionService = permissionService;
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

        Map<Long, String> storeNameMap = storeMapper.selectList(
                        new LambdaQueryWrapper<StoreEntity>().in(StoreEntity::getId, storeScope))
                .stream()
                .collect(Collectors.toMap(StoreEntity::getId, StoreEntity::getName, (left, right) -> left, LinkedHashMap::new));

        List<Long> categoryStoreIds = categories.stream()
                .map(ProductCategoryEntity::getStoreId)
                .distinct()
                .toList();
        Map<Long, Map<String, Long>> productCountMap = productMapper.selectList(
                        new LambdaQueryWrapper<ProductEntity>().in(ProductEntity::getStoreId, categoryStoreIds))
                .stream()
                .collect(Collectors.groupingBy(
                        ProductEntity::getStoreId,
                        LinkedHashMap::new,
                        Collectors.groupingBy(ProductEntity::getCategoryCode, LinkedHashMap::new, Collectors.counting())));

        return categories.stream()
                .map(category -> new ProductCategorySummary(
                        category.getId(),
                        category.getStoreId(),
                        storeNameMap.getOrDefault(category.getStoreId(), ""),
                        category.getCategoryName(),
                        category.getCategoryCode(),
                        defaultInt(category.getSortOrder()),
                        Boolean.TRUE.equals(category.getEnabled()),
                        resolveCount(productCountMap, category.getStoreId(), category.getCategoryCode())))
                .toList();
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
