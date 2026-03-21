package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.common.exception.BadRequestException;
import com.vdamo.ordering.common.exception.NotFoundException;
import com.vdamo.ordering.common.support.IdGenerator;
import com.vdamo.ordering.entity.SysMenuEntity;
import com.vdamo.ordering.entity.SysRoleMenuEntity;
import com.vdamo.ordering.mapper.SysMenuMapper;
import com.vdamo.ordering.mapper.SysRoleMenuMapper;
import com.vdamo.ordering.model.MenuSummary;
import com.vdamo.ordering.model.MenuUpsertRequest;
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
    private final PermissionService permissionService;
    private final IdGenerator idGenerator;

    public MenuService(
            SysMenuMapper sysMenuMapper,
            SysRoleMenuMapper sysRoleMenuMapper,
            PermissionService permissionService,
            IdGenerator idGenerator
    ) {
        this.sysMenuMapper = sysMenuMapper;
        this.sysRoleMenuMapper = sysRoleMenuMapper;
        this.permissionService = permissionService;
        this.idGenerator = idGenerator;
    }

    public List<MenuSummary> listCurrentByRoleIds(List<Long> roleIds) {
        return toSummaries(listEntitiesByRoleIds(roleIds).stream()
                .filter(menu -> StringUtils.hasText(menu.getRoute()))
                .toList());
    }

    public List<MenuSummary> listByRoleIds(List<Long> roleIds) {
        return toSummaries(listEntitiesByRoleIds(roleIds));
    }

    public List<String> listPermissionCodesByRoleIds(List<Long> roleIds) {
        return listEntitiesByRoleIds(roleIds).stream()
                .map(SysMenuEntity::getPermissionCode)
                .filter(StringUtils::hasText)
                .distinct()
                .sorted()
                .toList();
    }

    public List<MenuSummary> listAll(String keyword) {
        LambdaQueryWrapper<SysMenuEntity> wrapper = new LambdaQueryWrapper<SysMenuEntity>()
                .orderByAsc(SysMenuEntity::getSortOrder)
                .orderByAsc(SysMenuEntity::getId);
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

    public MenuSummary create(MenuUpsertRequest request) {
        SysMenuEntity entity = new SysMenuEntity();
        entity.setId(idGenerator.nextId());
        applyMenuValues(entity, request);
        sysMenuMapper.insert(entity);
        return toSummary(entity);
    }

    public MenuSummary update(Long id, MenuUpsertRequest request) {
        SysMenuEntity entity = requireMenu(id);
        applyMenuValues(entity, request);
        sysMenuMapper.updateById(entity);
        return toSummary(entity);
    }

    private List<SysMenuEntity> listEntitiesByRoleIds(List<Long> roleIds) {
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
                .filter(menu -> menu != null)
                .sorted(Comparator.comparing(SysMenuEntity::getSortOrder).thenComparing(SysMenuEntity::getId))
                .toList();
    }

    private void applyMenuValues(SysMenuEntity entity, MenuUpsertRequest request) {
        Long parentId = request.parentId();
        if (parentId != null) {
            if (entity.getId() != null && entity.getId().equals(parentId)) {
                throw new BadRequestException("Menu parent cannot be itself");
            }
            requireMenu(parentId);
        }

        String name = request.name().trim();
        String route = StringUtils.hasText(request.route()) ? request.route().trim() : "";
        String permissionCode = request.permissionCode().trim();

        validatePermissionCodeUnique(entity.getId(), permissionCode);
        validateRouteUnique(entity.getId(), route);

        entity.setParentId(parentId);
        entity.setName(name);
        entity.setRoute(route);
        entity.setPermissionCode(permissionCode);
        entity.setSortOrder(request.sortOrder());
        entity.setUpdater(permissionService.currentUser().username());
    }

    private void validatePermissionCodeUnique(Long currentId, String permissionCode) {
        List<SysMenuEntity> duplicates = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenuEntity>().eq(SysMenuEntity::getPermissionCode, permissionCode));
        boolean exists = duplicates.stream().anyMatch(item -> !item.getId().equals(currentId));
        if (exists) {
            throw new BadRequestException("Permission code already exists");
        }
    }

    private void validateRouteUnique(Long currentId, String route) {
        if (!StringUtils.hasText(route)) {
            return;
        }
        List<SysMenuEntity> duplicates = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenuEntity>().eq(SysMenuEntity::getRoute, route));
        boolean exists = duplicates.stream().anyMatch(item -> !item.getId().equals(currentId));
        if (exists) {
            throw new BadRequestException("Route already exists");
        }
    }

    private SysMenuEntity requireMenu(Long id) {
        SysMenuEntity entity = sysMenuMapper.selectById(id);
        if (entity == null) {
            throw new NotFoundException("Menu not found");
        }
        return entity;
    }

    private MenuSummary toSummary(SysMenuEntity entity) {
        return toSummaries(List.of(entity)).get(0);
    }

    private List<MenuSummary> toSummaries(List<SysMenuEntity> menus) {
        if (menus.isEmpty()) {
            return List.of();
        }

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
