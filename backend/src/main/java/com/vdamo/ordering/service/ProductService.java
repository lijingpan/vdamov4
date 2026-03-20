package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.entity.ProductEntity;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.mapper.ProductMapper;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.model.ProductSummary;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final StoreMapper storeMapper;
    private final PermissionService permissionService;

    public ProductService(
            ProductMapper productMapper,
            StoreMapper storeMapper,
            PermissionService permissionService
    ) {
        this.productMapper = productMapper;
        this.storeMapper = storeMapper;
        this.permissionService = permissionService;
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
            wrapper.eq(ProductEntity::getCategoryCode, categoryCode.trim());
        }
        if (StringUtils.hasText(keyword)) {
            String keywordValue = keyword.trim();
            wrapper.and(q -> q.like(ProductEntity::getName, keywordValue)
                    .or()
                    .like(ProductEntity::getCode, keywordValue)
                    .or()
                    .like(ProductEntity::getCategoryCode, keywordValue));
        }

        Map<Long, String> storeNameMap = storeMapper.selectList(
                        new LambdaQueryWrapper<StoreEntity>().in(StoreEntity::getId, storeScope))
                .stream()
                .collect(Collectors.toMap(StoreEntity::getId, StoreEntity::getName, (left, right) -> left));

        return productMapper.selectList(wrapper)
                .stream()
                .map(entity -> new ProductSummary(
                        entity.getId(),
                        entity.getStoreId(),
                        storeNameMap.getOrDefault(entity.getStoreId(), ""),
                        entity.getName(),
                        entity.getCode(),
                        entity.getCategoryCode(),
                        entity.getPriceInCent() == null ? 0 : entity.getPriceInCent(),
                        Boolean.TRUE.equals(entity.getActive())))
                .toList();
    }
}
