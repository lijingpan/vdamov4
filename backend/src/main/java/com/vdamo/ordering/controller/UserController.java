package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.UserSummary;
import com.vdamo.ordering.service.PermissionService;
import com.vdamo.ordering.service.UserService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
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
        permissionService.assertSuperAdmin();
        return ApiResponse.success(messageHelper.get("success.fetch"), userService.listAll(keyword, enabled, storeId));
    }
}
