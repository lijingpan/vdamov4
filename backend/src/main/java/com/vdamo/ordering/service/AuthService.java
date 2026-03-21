package com.vdamo.ordering.service;

import com.vdamo.ordering.common.exception.UnauthorizedException;
import com.vdamo.ordering.entity.SysRoleEntity;
import com.vdamo.ordering.entity.SysUserEntity;
import com.vdamo.ordering.model.AuthLoginRequest;
import com.vdamo.ordering.model.AuthLoginResponse;
import com.vdamo.ordering.model.AuthenticatedUser;
import com.vdamo.ordering.model.MenuSummary;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final RoleService roleService;
    private final MenuService menuService;
    private final ConcurrentHashMap<String, AuthenticatedUser> tokenStore = new ConcurrentHashMap<>();

    public AuthService(UserService userService, RoleService roleService, MenuService menuService) {
        this.userService = userService;
        this.roleService = roleService;
        this.menuService = menuService;
    }

    public AuthLoginResponse login(AuthLoginRequest request) {
        SysUserEntity user = userService.findByUsername(request.username());
        if (user == null || !Boolean.TRUE.equals(user.getEnabled()) || !user.getPassword().equals(request.password())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        List<SysRoleEntity> roles = roleService.listEntitiesByUserId(user.getId());
        List<Long> roleIds = roles.stream().map(SysRoleEntity::getId).toList();
        List<String> roleCodes = roles.stream().map(SysRoleEntity::getCode).toList();
        List<Long> storeIds = userService.listStoreIds(user.getId());
        List<String> permissionCodes = menuService.listPermissionCodesByRoleIds(roleIds);
        List<MenuSummary> menus = menuService.listCurrentByRoleIds(roleIds);

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                roleCodes,
                storeIds,
                permissionCodes
        );
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, authenticatedUser);
        return new AuthLoginResponse(token, authenticatedUser, menus);
    }

    public AuthenticatedUser getByToken(String token) {
        AuthenticatedUser user = tokenStore.get(token);
        if (user == null) {
            throw new UnauthorizedException("Token expired or invalid");
        }
        return user;
    }

    public void logout(String token) {
        tokenStore.remove(token);
    }
}
