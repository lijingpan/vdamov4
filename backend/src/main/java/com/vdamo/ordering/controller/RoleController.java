package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.RoleSummary;
import com.vdamo.ordering.model.RoleUpsertRequest;
import com.vdamo.ordering.service.PermissionService;
import com.vdamo.ordering.service.RoleService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final MessageHelper messageHelper;
    private final RoleService roleService;
    private final PermissionService permissionService;

    public RoleController(
            MessageHelper messageHelper,
            RoleService roleService,
            PermissionService permissionService
    ) {
        this.messageHelper = messageHelper;
        this.roleService = roleService;
        this.permissionService = permissionService;
    }

    @GetMapping
    public ApiResponse<List<RoleSummary>> list(@RequestParam(required = false) String keyword) {
        permissionService.assertPermission("role:view");
        return ApiResponse.success(messageHelper.get("success.fetch"), roleService.listAll(keyword));
    }

    @PostMapping
    public ApiResponse<RoleSummary> create(@Valid @RequestBody RoleUpsertRequest request) {
        permissionService.assertPermission("role:create");
        return ApiResponse.success(messageHelper.get("success.fetch"), roleService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoleSummary> update(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpsertRequest request
    ) {
        permissionService.assertPermission("role:update");
        return ApiResponse.success(messageHelper.get("success.fetch"), roleService.update(id, request));
    }
}
