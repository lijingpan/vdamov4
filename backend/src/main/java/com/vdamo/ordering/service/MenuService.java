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

    private static final String MENU_TYPE_MENU = "MENU";
    private static final String MENU_TYPE_BUTTON = "BUTTON";

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
                .filter(menu -> MENU_TYPE_MENU.equals(resolveMenuType(menu)))
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

    public void delete(Long id) {
        SysMenuEntity entity = requireMenu(id);

        Long childCount = sysMenuMapper.selectCount(
                new LambdaQueryWrapper<SysMenuEntity>().eq(SysMenuEntity::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BadRequestException("Please delete child menus first");
        }

        Long roleBindingCount = sysRoleMenuMapper.selectCount(
                new LambdaQueryWrapper<SysRoleMenuEntity>().eq(SysRoleMenuEntity::getMenuId, id));
        if (roleBindingCount != null && roleBindingCount > 0) {
            throw new BadRequestException("Menu is assigned to roles and cannot be deleted");
        }

        sysMenuMapper.deleteById(entity.getId());
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
        String menuType = normalizeMenuType(request.menuType(), request.route());
        Long parentId = request.parentId();
        if (parentId != null) {
            if (entity.getId() != null && entity.getId().equals(parentId)) {
                throw new BadRequestException("Menu parent cannot be itself");
            }
            SysMenuEntity parent = requireMenu(parentId);
            if (MENU_TYPE_BUTTON.equals(resolveMenuType(parent))) {
                throw new BadRequestException("Button cannot be parent menu");
            }
        }
        if (MENU_TYPE_BUTTON.equals(menuType) && parentId == null) {
            throw new BadRequestException("Button must have a parent menu");
        }

        String name = request.name().trim();
        String route = StringUtils.hasText(request.route()) ? request.route().trim() : "";
        String permissionCode = request.permissionCode().trim();
        if (MENU_TYPE_MENU.equals(menuType) && !StringUtils.hasText(route)) {
            throw new BadRequestException("Menu route is required");
        }
        if (MENU_TYPE_BUTTON.equals(menuType)) {
            if (hasChildren(entity.getId())) {
                throw new BadRequestException("Button menu cannot have child menus");
            }
            route = "";
        }

        validatePermissionCodeUnique(entity.getId(), permissionCode);
        validateRouteUnique(entity.getId(), route);

        entity.setParentId(parentId);
        entity.setName(name);
        entity.setMenuType(menuType);
        entity.setRoute(route);
        entity.setPermissionCode(permissionCode);
        entity.setSortOrder(request.sortOrder());
        entity.setUpdater(permissionService.currentUser().username());
    }

    private boolean hasChildren(Long id) {
        if (id == null) {
            return false;
        }
        Long childCount = sysMenuMapper.selectCount(
                new LambdaQueryWrapper<SysMenuEntity>().eq(SysMenuEntity::getParentId, id));
        return childCount != null && childCount > 0;
    }

    private String normalizeMenuType(String menuTypeValue, String routeValue) {
        if (!StringUtils.hasText(menuTypeValue)) {
            return StringUtils.hasText(routeValue) ? MENU_TYPE_MENU : MENU_TYPE_BUTTON;
        }
        String normalized = menuTypeValue.trim().toUpperCase();
        if (!MENU_TYPE_MENU.equals(normalized) && !MENU_TYPE_BUTTON.equals(normalized)) {
            throw new BadRequestException("Menu type must be MENU or BUTTON");
        }
        return normalized;
    }

    private String resolveMenuType(SysMenuEntity entity) {
        return normalizeMenuType(entity.getMenuType(), entity.getRoute());
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
                        resolveMenuType(menu),
                        menu.getRoute(),
                        menu.getPermissionCode(),
                        menu.getSortOrder()))
                .toList();
    }
}
