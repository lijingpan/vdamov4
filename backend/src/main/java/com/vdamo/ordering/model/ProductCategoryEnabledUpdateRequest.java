package com.vdamo.ordering.model;

import jakarta.validation.constraints.NotNull;

public record ProductCategoryEnabledUpdateRequest(
        @NotNull(message = "Category enabled flag is required")
        Boolean enabled
) {
}
