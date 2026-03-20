package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.entity.SysMenuEntity;
import com.vdamo.ordering.entity.SysRoleMenuEntity;
import com.vdamo.ordering.mapper.SysMenuMapper;
import com.vdamo.ordering.mapper.SysRoleMenuMapper;
import com.vdamo.ordering.model.MenuSummary;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

        List<SysMenuEntity> menus = sysMenuMapper.selectBatchIds(menuIds).stream()
                .filter(menu -> menu != null)
                .sorted(Comparator.comparing(SysMenuEntity::getSortOrder))
                .toList();
        return toSummaries(menus);
    }

    public List<MenuSummary> listAll(String keyword) {
        LambdaQueryWrapper<SysMenuEntity> wrapper = new LambdaQueryWrapper<SysMenuEntity>()
                .orderByAsc(SysMenuEntity::getSortOrder);
        if (StringUtils.hasText(keyword)) {
            String keywordValue = keyword.trim();
            wrapper.and(query -> query.like(SysMenuEntity::getName, keywordValue)
                    .or()
                    .like(SysMenuEntity::getRoute, keywordValue)
                    .or()
                    .like(SysMenuEntity::getPermissionCode, keywordValue));
        }
        List<SysMenuEntity> menus = sysMenuMapper.selectList(wrapper);
        if (menus.isEmpty()) {
            return List.of();
        }
        return toSummaries(menus);
    }

    private List<MenuSummary> toSummaries(List<SysMenuEntity> menus) {
        Set<Long> parentIds = menus.stream()
                .map(SysMenuEntity::getParentId)
                .filter(parentId -> parentId != null)
                .collect(Collectors.toSet());

        Map<Long, String> parentNameById = parentIds.isEmpty()
                ? Map.of()
                : sysMenuMapper.selectBatchIds(parentIds).stream()
                        .filter(parent -> parent != null)
                        .collect(Collectors.toMap(SysMenuEntity::getId, SysMenuEntity::getName, (left, right) -> left));

        return menus.stream()
                .map(menu -> new MenuSummary(
                        menu.getId(),
                        menu.getParentId(),
                        menu.getParentId() == null ? null : parentNameById.getOrDefault(menu.getParentId(), ""),
                        menu.getName(),
                        menu.getRoute(),
                        menu.getPermissionCode(),
                        menu.getSortOrder()))
                .toList();
    }
}
