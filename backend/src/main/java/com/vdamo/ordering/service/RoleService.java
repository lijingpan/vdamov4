package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.common.exception.BadRequestException;
import com.vdamo.ordering.common.exception.NotFoundException;
import com.vdamo.ordering.common.support.IdGenerator;
import com.vdamo.ordering.entity.SysMenuEntity;
import com.vdamo.ordering.entity.SysRoleEntity;
import com.vdamo.ordering.entity.SysRoleMenuEntity;
import com.vdamo.ordering.entity.SysUserRoleEntity;
import com.vdamo.ordering.mapper.SysMenuMapper;
import com.vdamo.ordering.mapper.SysRoleMapper;
import com.vdamo.ordering.mapper.SysRoleMenuMapper;
import com.vdamo.ordering.mapper.SysUserRoleMapper;
import com.vdamo.ordering.model.RoleSummary;
import com.vdamo.ordering.model.RoleUpsertRequest;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RoleService {

    private static final String SUPER_ADMIN_ROLE_CODE = "SUPER_ADMIN";

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysMenuMapper sysMenuMapper;
    private final PermissionService permissionService;
    private final IdGenerator idGenerator;

    public RoleService(
            SysRoleMapper sysRoleMapper,
            SysRoleMenuMapper sysRoleMenuMapper,
            SysUserRoleMapper sysUserRoleMapper,
            SysMenuMapper sysMenuMapper,
            PermissionService permissionService,
            IdGenerator idGenerator
    ) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysRoleMenuMapper = sysRoleMenuMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysMenuMapper = sysMenuMapper;
        this.permissionService = permissionService;
        this.idGenerator = idGenerator;
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
                            permissionCodes,
                            roleMenuIds.stream().sorted().toList());
                })
                .toList();
    }

    public RoleSummary getById(Long id) {
        return getSummary(id);
    }

    public RoleSummary create(RoleUpsertRequest request) {
        SysRoleEntity entity = new SysRoleEntity();
        entity.setId(idGenerator.nextId());
        applyRoleValues(entity, request);
        sysRoleMapper.insert(entity);
        syncRoleMenus(entity.getId(), request.menuIds());
        return getSummary(entity.getId());
    }

    public RoleSummary getById(Long id) {
        return getSummary(id);
    }

    public RoleSummary update(Long id, RoleUpsertRequest request) {
        SysRoleEntity entity = requireRole(id);
        if (SUPER_ADMIN_ROLE_CODE.equalsIgnoreCase(entity.getCode())) {
            throw new BadRequestException("Super admin role cannot be modified");
        }
        applyRoleValues(entity, request);
        sysRoleMapper.updateById(entity);
        syncRoleMenus(id, request.menuIds());
        return getSummary(id);
    }

    public void delete(Long id) {
        SysRoleEntity entity = requireRole(id);
        if (SUPER_ADMIN_ROLE_CODE.equalsIgnoreCase(entity.getCode())) {
            throw new BadRequestException("Super admin role cannot be deleted");
        }

        Long userBindingCount = sysUserRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRoleEntity>().eq(SysUserRoleEntity::getRoleId, id));
        if (userBindingCount != null && userBindingCount > 0) {
            throw new BadRequestException("Role is assigned to users and cannot be deleted");
        }

        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenuEntity>().eq(SysRoleMenuEntity::getRoleId, id));
        sysRoleMapper.deleteById(entity.getId());
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

    private RoleSummary getSummary(Long id) {
        return listAll(null).stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Role not found"));
    }

    private void applyRoleValues(SysRoleEntity entity, RoleUpsertRequest request) {
        String code = request.code().trim().toUpperCase();
        String name = request.name().trim();
        List<Long> menuIds = normalizeIds(request.menuIds());

        validateRoleCodeUnique(entity.getId(), code);
        validateMenuIds(menuIds);

        entity.setCode(code);
        entity.setName(name);
        entity.setUpdater(permissionService.currentUser().username());
    }

    private void validateRoleCodeUnique(Long currentId, String code) {
        List<SysRoleEntity> duplicates = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRoleEntity>().eq(SysRoleEntity::getCode, code));
        boolean exists = duplicates.stream().anyMatch(item -> !item.getId().equals(currentId));
        if (exists) {
            throw new BadRequestException("Role code already exists");
        }
    }

    private void validateMenuIds(List<Long> menuIds) {
        if (menuIds.isEmpty()) {
            throw new BadRequestException("At least one menu is required");
        }
        long existingCount = sysMenuMapper.selectBatchIds(menuIds).stream()
                .filter(item -> item != null)
                .count();
        if (existingCount != menuIds.size()) {
            throw new BadRequestException("Some menus do not exist");
        }
    }

    private void syncRoleMenus(Long roleId, List<Long> menuIds) {
        List<Long> normalizedMenuIds = normalizeIds(menuIds);
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenuEntity>().eq(SysRoleMenuEntity::getRoleId, roleId));
        String username = permissionService.currentUser().username();
        normalizedMenuIds.forEach(menuId -> {
            SysRoleMenuEntity entity = new SysRoleMenuEntity();
            entity.setId(idGenerator.nextId());
            entity.setRoleId(roleId);
            entity.setMenuId(menuId);
            entity.setUpdater(username);
            sysRoleMenuMapper.insert(entity);
        });
    }

    private SysRoleEntity requireRole(Long id) {
        SysRoleEntity entity = sysRoleMapper.selectById(id);
        if (entity == null) {
            throw new NotFoundException("Role not found");
        }
        return entity;
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream().filter(item -> item != null).distinct().toList();
    }
}
