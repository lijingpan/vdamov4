package com.vdamo.ordering.model;

import java.util.List;

public record UserSummary(
        Long id,
        String username,
        String displayName,
        boolean enabled,
        List<String> roleCodes,
        List<Long> storeIds
) {
}

