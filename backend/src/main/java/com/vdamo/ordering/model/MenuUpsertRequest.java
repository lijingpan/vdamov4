package com.vdamo.ordering.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MenuUpsertRequest(
        Long parentId,
        @NotBlank(message = "Menu name is required")
        String name,
        String menuType,
        String route,
        @NotBlank(message = "Permission code is required")
        String permissionCode,
        @NotNull(message = "Sort order is required")
        @Min(value = 0, message = "Sort order must be greater than or equal to 0")
        Integer sortOrder
) {
}
