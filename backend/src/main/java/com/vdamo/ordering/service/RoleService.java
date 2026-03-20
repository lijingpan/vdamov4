package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.entity.SysMenuEntity;
import com.vdamo.ordering.entity.SysRoleEntity;
import com.vdamo.ordering.entity.SysRoleMenuEntity;
import com.vdamo.ordering.entity.SysUserRoleEntity;
import com.vdamo.ordering.mapper.SysRoleMapper;
import com.vdamo.ordering.mapper.SysRoleMenuMapper;
import com.vdamo.ordering.mapper.SysMenuMapper;
import com.vdamo.ordering.mapper.SysUserRoleMapper;
import com.vdamo.ordering.model.RoleSummary;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysMenuMapper sysMenuMapper;

    public RoleService(
            SysRoleMapper sysRoleMapper,
            SysRoleMenuMapper sysRoleMenuMapper,
            SysUserRoleMapper sysUserRoleMapper,
            SysMenuMapper sysMenuMapper
    ) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysRoleMenuMapper = sysRoleMenuMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysMenuMapper = sysMenuMapper;
    }

    public List<RoleSummary> listAll(String keyword) {
        LambdaQueryWrapper<SysRoleEntity> wrapper = new LambdaQueryWrapper<SysRoleEntity>()
                .orderByAsc(SysRoleEntity::getId);
        if (StringUtils.hasText(keyword)) {
            String keywordValue = keyword.trim();
            wrapper.and(query -> query.like(SysRoleEntity::getCode, keywordValue)
                    .or()
                    .like(SysRoleEntity::getName, keywordValue));
        }

        List<SysRoleEntity> roles = sysRoleMapper.selectList(wrapper);
        if (roles.isEmpty()) {
            return List.of();
        }

        List<Long> roleIds = roles.stream().map(SysRoleEntity::getId).toList();
        List<SysRoleMenuEntity> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenuEntity>().in(SysRoleMenuEntity::getRoleId, roleIds));
        List<SysUserRoleEntity> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRoleEntity>().in(SysUserRoleEntity::getRoleId, roleIds));

        Set<Long> menuIds = roleMenus.stream().map(SysRoleMenuEntity::getMenuId).collect(Collectors.toSet());
        Map<Long, String> permissionCodeByMenuId = menuIds.isEmpty()
                ? Map.of()
                : sysMenuMapper.selectBatchIds(menuIds).stream()
                        .filter(menu -> menu != null)
                        .filter(menu -> StringUtils.hasText(menu.getPermissionCode()))
                        .collect(Collectors.toMap(
                                SysMenuEntity::getId,
                                SysMenuEntity::getPermissionCode,
                                (left, right) -> left));

        Map<Long, Set<Long>> menuIdsByRoleId = roleMenus.stream().collect(Collectors.groupingBy(
                SysRoleMenuEntity::getRoleId,
                Collectors.mapping(SysRoleMenuEntity::getMenuId, Collectors.toSet())));
        Map<Long, Set<Long>> userIdsByRoleId = userRoles.stream().collect(Collectors.groupingBy(
                SysUserRoleEntity::getRoleId,
                Collectors.mapping(SysUserRoleEntity::getUserId, Collectors.toSet())));

        return roles.stream()
                .map(role -> {
                    Set<Long> roleMenuIds = menuIdsByRoleId.getOrDefault(role.getId(), Set.of());
                    List<String> permissionCodes = roleMenuIds.stream()
                            .map(permissionCodeByMenuId::get)
                            .filter(StringUtils::hasText)
                            .distinct()
                            .sorted(Comparator.naturalOrder())
                            .toList();
                    int userCount = userIdsByRoleId.getOrDefault(role.getId(), Set.of()).size();
                    return new RoleSummary(
                            role.getId(),
                            role.getCode(),
                            role.getName(),
                            roleMenuIds.size(),
                            userCount,
                            permissionCodes);
                })
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
