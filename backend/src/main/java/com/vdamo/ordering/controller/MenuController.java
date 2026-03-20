package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.MenuSummary;
import com.vdamo.ordering.service.MenuService;
import com.vdamo.ordering.service.PermissionService;
import com.vdamo.ordering.service.RoleService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
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
        Long userId = permissionService.currentUser().userId();
        List<Long> roleIds = roleService.listEntitiesByUserId(userId).stream().map(role -> role.getId()).toList();
        return ApiResponse.success(messageHelper.get("success.fetch"), menuService.listByRoleIds(roleIds));
    }

    @GetMapping
    public ApiResponse<List<MenuSummary>> listAll(@RequestParam(required = false) String keyword) {
        permissionService.assertSuperAdmin();
        return ApiResponse.success(messageHelper.get("success.fetch"), menuService.listAll(keyword));
    }
}
