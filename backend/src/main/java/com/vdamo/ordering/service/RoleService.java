package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.entity.SysRoleEntity;
import com.vdamo.ordering.entity.SysUserRoleEntity;
import com.vdamo.ordering.mapper.SysRoleMapper;
import com.vdamo.ordering.mapper.SysUserRoleMapper;
import com.vdamo.ordering.model.RoleSummary;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    public RoleService(SysRoleMapper sysRoleMapper, SysUserRoleMapper sysUserRoleMapper) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
    }

    public List<RoleSummary> listAll() {
        return sysRoleMapper.selectList(new LambdaQueryWrapper<SysRoleEntity>()).stream()
                .map(role -> new RoleSummary(role.getId(), role.getCode(), role.getName()))
                .toList();
    }

    public List<SysRoleEntity> listEntitiesByUserId(Long userId) {
        List<Long> roleIds = sysUserRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRoleEntity>().eq(SysUserRoleEntity::getUserId, userId))
                .stream()
                .map(SysUserRoleEntity::getRoleId)
                .toList();

        if (roleIds.isEmpty()) {
            return List.of();
        }
        return sysRoleMapper.selectBatchIds(roleIds);
    }
}

