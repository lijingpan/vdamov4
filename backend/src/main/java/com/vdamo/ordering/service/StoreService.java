package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.model.StoreSummary;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StoreService {

    private final StoreMapper storeMapper;
    private final PermissionService permissionService;

    public StoreService(StoreMapper storeMapper, PermissionService permissionService) {
        this.storeMapper = storeMapper;
        this.permissionService = permissionService;
    }

    public List<StoreSummary> list() {
        return storeMapper.selectList(
                        new LambdaQueryWrapper<StoreEntity>()
                                .in(StoreEntity::getId, permissionService.currentStoreIds()))
                .stream()
                .map(entity -> new StoreSummary(
                        entity.getId(),
                        entity.getName(),
                        entity.getCountryCode(),
                        entity.getStatus(),
                        List.of(entity.getBusinessModes().split(","))))
                .toList();
    }
}
