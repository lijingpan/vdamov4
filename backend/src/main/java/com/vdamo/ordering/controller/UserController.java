package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.UserSummary;
import com.vdamo.ordering.model.UserUpsertRequest;
import com.vdamo.ordering.service.PermissionService;
import com.vdamo.ordering.service.UserService;
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
@RequestMapping("/api/v1/users")
public class UserController {

    private final MessageHelper messageHelper;
    private final UserService userService;
    private final PermissionService permissionService;

    public UserController(
            MessageHelper messageHelper,
            UserService userService,
            PermissionService permissionService
    ) {
        this.messageHelper = messageHelper;
        this.userService = userService;
        this.permissionService = permissionService;
    }

    @GetMapping
    public ApiResponse<List<UserSummary>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Long storeId
    ) {
        permissionService.assertPermission("user:view");
        return ApiResponse.success(messageHelper.get("success.fetch"), userService.listAll(keyword, enabled, storeId));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserSummary> detail(@PathVariable Long id) {
        permissionService.assertPermission("user:view");
        return ApiResponse.success(messageHelper.get("success.fetch"), userService.getById(id));
    }

    @PostMapping
    public ApiResponse<UserSummary> create(@Valid @RequestBody UserUpsertRequest request) {
        permissionService.assertPermission("user:create");
        return ApiResponse.success(messageHelper.get("success.fetch"), userService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserSummary> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpsertRequest request
    ) {
        permissionService.assertPermission("user:update");
        return ApiResponse.success(messageHelper.get("success.fetch"), userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        permissionService.assertPermission("user:delete");
        userService.delete(id);
        return ApiResponse.success(messageHelper.get("success.fetch"), null);
    }
}
