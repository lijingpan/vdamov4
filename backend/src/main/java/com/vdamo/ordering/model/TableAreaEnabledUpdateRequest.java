package com.vdamo.ordering.model;

import jakarta.validation.constraints.NotNull;

public record TableAreaEnabledUpdateRequest(
        @NotNull(message = "Enabled flag is required")
        Boolean enabled
) {
}
