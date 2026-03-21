package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.MenuUpsertRequest;
import com.vdamo.ordering.model.MenuSummary;
import com.vdamo.ordering.service.MenuService;
import com.vdamo.ordering.service.PermissionService;
import com.vdamo.ordering.service.RoleService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/menus")
public class MenuController {

    private final MessageHelper messageHelper;
    private final MenuService menuService;
    private final PermissionService permissionService;
    private final RoleService roleService;

    public MenuController(
            MessageHelper messageHelper,
            MenuService menuService,
            PermissionService permissionService,
            RoleService roleService
    ) {
        this.messageHelper = messageHelper;
        this.menuService = menuService;
        this.permissionService = permissionService;
        this.roleService = roleService;
    }

    @GetMapping("/current")
    public ApiResponse<List<MenuSummary>> currentMenus() {
        if (permissionService.currentUser().roleCodes().contains("SUPER_ADMIN")) {
            return ApiResponse.success(messageHelper.get("success.fetch"), menuService.listCurrentAll());
        }
        Long userId = permissionService.currentUser().userId();
        List<Long> roleIds = roleService.listEntitiesByUserId(userId).stream().map(role -> role.getId()).toList();
        return ApiResponse.success(messageHelper.get("success.fetch"), menuService.listCurrentByRoleIds(roleIds));
    }

    @GetMapping
    public ApiResponse<List<MenuSummary>> listAll(@RequestParam(required = false) String keyword) {
        permissionService.assertPermission("menu:view");
        return ApiResponse.success(messageHelper.get("success.fetch"), menuService.listAll(keyword));
    }

    @PostMapping
    public ApiResponse<MenuSummary> create(@Valid @RequestBody MenuUpsertRequest request) {
        permissionService.assertPermission("menu:create");
        return ApiResponse.success(messageHelper.get("success.fetch"), menuService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<MenuSummary> update(
            @PathVariable Long id,
            @Valid @RequestBody MenuUpsertRequest request
    ) {
        permissionService.assertPermission("menu:update");
        return ApiResponse.success(messageHelper.get("success.fetch"), menuService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        permissionService.assertPermission("menu:delete");
        menuService.delete(id);
        return ApiResponse.success(messageHelper.get("success.fetch"), null);
    }
}
