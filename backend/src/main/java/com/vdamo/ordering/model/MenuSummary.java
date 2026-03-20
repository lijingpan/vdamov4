package com.vdamo.ordering.model;

public record MenuSummary(
        Long id,
        Long parentId,
        String name,
        String route,
        String permissionCode,
        Integer sortOrder
) {
}

