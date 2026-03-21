package com.vdamo.ordering.model;

import jakarta.validation.constraints.NotNull;

public record DeviceEnabledUpdateRequest(
        @NotNull(message = "Device enabled flag is required")
        Boolean enabled
) {
}
