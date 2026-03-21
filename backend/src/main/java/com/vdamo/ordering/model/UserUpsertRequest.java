package com.vdamo.ordering.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UserUpsertRequest(
        @NotBlank(message = "Username is required")
        String username,
        String password,
        @NotBlank(message = "Display name is required")
        String displayName,
        @NotNull(message = "Enabled flag is required")
        Boolean enabled,
        @NotEmpty(message = "At least one role is required")
        List<Long> roleIds,
        @NotEmpty(message = "At least one store is required")
        List<Long> storeIds
) {
}
