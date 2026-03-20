package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.AuthLoginRequest;
import com.vdamo.ordering.model.AuthLoginResponse;
import com.vdamo.ordering.model.AuthenticatedUser;
import com.vdamo.ordering.service.AuthService;
import com.vdamo.ordering.service.PermissionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final MessageHelper messageHelper;
    private final AuthService authService;
    private final PermissionService permissionService;

    public AuthController(
            MessageHelper messageHelper,
            AuthService authService,
            PermissionService permissionService
    ) {
        this.messageHelper = messageHelper;
        this.authService = authService;
        this.permissionService = permissionService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthLoginResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        return ApiResponse.success(messageHelper.get("success.fetch"), authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<AuthenticatedUser> me() {
        return ApiResponse.success(messageHelper.get("success.fetch"), permissionService.currentUser());
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.startsWith(BEARER_PREFIX)
                ? authorization.substring(BEARER_PREFIX.length()).trim()
                : authorization;
        authService.logout(token);
        return ApiResponse.success(messageHelper.get("success.fetch"), null);
    }
}
