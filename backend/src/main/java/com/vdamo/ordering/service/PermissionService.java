package com.vdamo.ordering.service;

import com.vdamo.ordering.common.exception.ForbiddenException;
import com.vdamo.ordering.common.exception.UnauthorizedException;
import com.vdamo.ordering.common.security.UserContext;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.model.AuthenticatedUser;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PermissionService {

    private final StoreMapper storeMapper;

    public PermissionService(StoreMapper storeMapper) {
        this.storeMapper = storeMapper;
    }

    public AuthenticatedUser currentUser() {
        AuthenticatedUser user = UserContext.get();
        if (user == null) {
            throw new UnauthorizedException("No authenticated user");
        }
        return user;
    }

    public List<Long> currentStoreIds() {
        AuthenticatedUser user = currentUser();
        if (isSuperAdmin()) {
            return storeMapper.selectList(null).stream()
                    .map(StoreEntity::getId)
                    .toList();
        }
        return user.storeIds();
    }

    public List<String> currentPermissionCodes() {
        return currentUser().permissionCodes();
    }

    public boolean isSuperAdmin() {
        return currentUser().roleCodes().contains("SUPER_ADMIN");
    }

    public void assertSuperAdmin() {
        if (!isSuperAdmin()) {
            throw new ForbiddenException("Super admin permission required");
        }
    }

    public boolean hasPermission(String permissionCode) {
        if (!StringUtils.hasText(permissionCode)) {
            return true;
        }
        AuthenticatedUser user = currentUser();
        return user.roleCodes().contains("SUPER_ADMIN") || user.permissionCodes().contains(permissionCode);
    }

    public void assertPermission(String permissionCode) {
        if (!hasPermission(permissionCode)) {
            throw new ForbiddenException("Permission denied: " + permissionCode);
        }
    }

    public void assertStoreAccess(Long storeId) {
        if (storeId == null) {
            return;
        }
        if (isSuperAdmin()) {
            return;
        }
        if (!currentStoreIds().contains(storeId)) {
            throw new ForbiddenException("Store access denied");
        }
    }

    public Long resolveStoreId(Long storeId) {
        if (storeId != null) {
            assertStoreAccess(storeId);
            return storeId;
        }
        return currentStoreIds().stream()
                .findFirst()
                .orElseThrow(() -> new ForbiddenException("No store permission"));
    }
}
