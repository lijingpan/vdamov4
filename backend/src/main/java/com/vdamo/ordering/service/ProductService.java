package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.entity.ProductEntity;
import com.vdamo.ordering.mapper.ProductMapper;
import com.vdamo.ordering.model.ProductSummary;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final PermissionService permissionService;

    public ProductService(ProductMapper productMapper, PermissionService permissionService) {
        this.productMapper = productMapper;
        this.permissionService = permissionService;
    }

    public List<ProductSummary> list() {
        return productMapper.selectList(
                        new LambdaQueryWrapper<ProductEntity>()
                                .in(ProductEntity::getStoreId, permissionService.currentStoreIds()))
                .stream()
                .map(entity -> new ProductSummary(
                        entity.getId(),
                        entity.getName(),
                        entity.getCode(),
                        entity.getCategoryCode(),
                        entity.getPriceInCent(),
                        Boolean.TRUE.equals(entity.getActive())))
                .toList();
    }
}
