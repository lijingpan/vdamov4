package com.vdamo.ordering.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record RoleUpsertRequest(
        @NotBlank(message = "Role code is required")
        String code,
        @NotBlank(message = "Role name is required")
        String name,
        @NotEmpty(message = "At least one menu is required")
        List<Long> menuIds
) {
}
