package com.vdamo.ordering.model;

import jakarta.validation.constraints.NotNull;

public record ProductStatusUpdateRequest(
        @NotNull(message = "Product active flag is required")
        Boolean active
) {
}
