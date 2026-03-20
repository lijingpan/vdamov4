package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.entity.SysUserEntity;
import com.vdamo.ordering.entity.SysUserStoreEntity;
import com.vdamo.ordering.mapper.SysUserMapper;
import com.vdamo.ordering.mapper.SysUserStoreMapper;
import com.vdamo.ordering.model.UserSummary;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserStoreMapper sysUserStoreMapper;
    private final RoleService roleService;

    public UserService(
            SysUserMapper sysUserMapper,
            SysUserStoreMapper sysUserStoreMapper,
            RoleService roleService
    ) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserStoreMapper = sysUserStoreMapper;
        this.roleService = roleService;
    }

    public List<UserSummary> listAll() {
        return sysUserMapper.selectList(new LambdaQueryWrapper<SysUserEntity>()).stream()
                .map(this::toSummary)
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
        return new UserSummary(
                entity.getId(),
                entity.getUsername(),
                entity.getDisplayName(),
                Boolean.TRUE.equals(entity.getEnabled()),
                roleCodes,
                listStoreIds(entity.getId())
        );
    }
}

