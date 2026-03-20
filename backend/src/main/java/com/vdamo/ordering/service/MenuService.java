package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.entity.SysMenuEntity;
import com.vdamo.ordering.entity.SysRoleMenuEntity;
import com.vdamo.ordering.mapper.SysMenuMapper;
import com.vdamo.ordering.mapper.SysRoleMenuMapper;
import com.vdamo.ordering.model.MenuSummary;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MenuService {

    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    public MenuService(SysMenuMapper sysMenuMapper, SysRoleMenuMapper sysRoleMenuMapper) {
        this.sysMenuMapper = sysMenuMapper;
        this.sysRoleMenuMapper = sysRoleMenuMapper;
    }

    public List<MenuSummary> listByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }

        List<Long> menuIds = sysRoleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenuEntity>().in(SysRoleMenuEntity::getRoleId, roleIds))
                .stream()
                .map(SysRoleMenuEntity::getMenuId)
                .distinct()
                .toList();

        if (menuIds.isEmpty()) {
            return List.of();
        }

        return sysMenuMapper.selectBatchIds(menuIds).stream()
                .sorted(Comparator.comparing(SysMenuEntity::getSortOrder))
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public List<MenuSummary> listAll() {
        return sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenuEntity>()
                        .orderByAsc(SysMenuEntity::getSortOrder))
                .stream()
                .map(this::toSummary)
                .toList();
    }

    private MenuSummary toSummary(SysMenuEntity entity) {
        return new MenuSummary(
                entity.getId(),
                entity.getParentId(),
                entity.getName(),
                entity.getRoute(),
                entity.getPermissionCode(),
                entity.getSortOrder()
        );
    }
}

