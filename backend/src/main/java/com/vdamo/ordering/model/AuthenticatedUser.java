package com.vdamo.ordering.model;

import java.util.List;

public record AuthenticatedUser(
        Long userId,
        String username,
        String displayName,
        List<String> roleCodes,
        List<Long> storeIds
) {
}

