package com.vdamo.ordering.model;

import java.util.List;

public record RoleSummary(
        Long id,
        String code,
        String name,
        int menuCount,
        int userCount,
        List<String> permissionCodes
) {
}
