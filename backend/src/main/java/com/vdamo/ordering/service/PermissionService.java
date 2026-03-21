package com.vdamo.ordering.service;

import com.vdamo.ordering.common.exception.ForbiddenException;
import com.vdamo.ordering.common.exception.UnauthorizedException;
import com.vdamo.ordering.common.security.UserContext;
import com.vdamo.ordering.model.AuthenticatedUser;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PermissionService {

    public AuthenticatedUser currentUser() {
        AuthenticatedUser user = UserContext.get();
        if (user == null) {
            throw new UnauthorizedException("No authenticated user");
        }
        return user;
    }

    public List<Long> currentStoreIds() {
        return currentUser().storeIds();
    }

    public List<String> currentPermissionCodes() {
        return currentUser().permissionCodes();
    }

    public void assertSuperAdmin() {
        if (!currentUser().roleCodes().contains("SUPER_ADMIN")) {
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
