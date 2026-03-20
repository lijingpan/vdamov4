package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.entity.SysUserEntity;
import com.vdamo.ordering.entity.SysUserStoreEntity;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.mapper.SysUserMapper;
import com.vdamo.ordering.mapper.SysUserStoreMapper;
import com.vdamo.ordering.model.UserSummary;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserStoreMapper sysUserStoreMapper;
    private final StoreMapper storeMapper;
    private final RoleService roleService;

    public UserService(
            SysUserMapper sysUserMapper,
            SysUserStoreMapper sysUserStoreMapper,
            StoreMapper storeMapper,
            RoleService roleService
    ) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserStoreMapper = sysUserStoreMapper;
        this.storeMapper = storeMapper;
        this.roleService = roleService;
    }

    public List<UserSummary> listAll(String keyword, Boolean enabled, Long storeId) {
        LambdaQueryWrapper<SysUserEntity> wrapper = new LambdaQueryWrapper<SysUserEntity>()
                .orderByAsc(SysUserEntity::getId);
        if (enabled != null) {
            wrapper.eq(SysUserEntity::getEnabled, enabled);
        }
        if (StringUtils.hasText(keyword)) {
            String keywordValue = keyword.trim();
            wrapper.and(query -> query.like(SysUserEntity::getUsername, keywordValue)
                    .or()
                    .like(SysUserEntity::getDisplayName, keywordValue));
        }

        List<UserSummary> summaries = sysUserMapper.selectList(wrapper).stream()
                .map(this::toSummary)
                .toList();
        if (storeId == null) {
            return summaries;
        }
        return summaries.stream()
                .filter(summary -> summary.storeIds().contains(storeId))
                .toList();
    }

    public SysUserEntity findByUsername(String username) {
        return sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getUsername, username)
        );
    }

    public List<Long> listStoreIds(Long userId) {
        return sysUserStoreMapper.selectList(
                        new LambdaQueryWrapper<SysUserStoreEntity>().eq(SysUserStoreEntity::getUserId, userId))
                .stream()
                .map(SysUserStoreEntity::getStoreId)
                .toList();
    }

    public UserSummary toSummary(SysUserEntity entity) {
        List<String> roleCodes = roleService.listEntitiesByUserId(entity.getId()).stream()
                .map(role -> role.getCode())
                .toList();
        List<Long> storeIds = listStoreIds(entity.getId());
        Map<Long, String> storeNameById = storeIds.isEmpty()
                ? Map.of()
                : storeMapper.selectBatchIds(storeIds).stream()
                        .filter(store -> store != null)
                        .collect(Collectors.toMap(StoreEntity::getId, StoreEntity::getName, (left, right) -> left));
        List<String> storeNames = storeIds.isEmpty()
                ? List.of()
                : storeIds.stream()
                        .map(id -> storeNameById.getOrDefault(id, ""))
                        .filter(StringUtils::hasText)
                        .toList();
        return new UserSummary(
                entity.getId(),
                entity.getUsername(),
                entity.getDisplayName(),
                Boolean.TRUE.equals(entity.getEnabled()),
                roleCodes,
                storeIds,
                storeNames
        );
    }
}
