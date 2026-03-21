package com.vdamo.ordering.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeviceUpsertRequest(
        @NotNull(message = "Store id is required")
        Long storeId,
        @NotBlank(message = "Device name is required")
        String name,
        @NotBlank(message = "Device type is required")
        String type,
        String purpose,
        String brand,
        String sn,
        String size,
        String onlineStatus,
        @NotNull(message = "Device enabled flag is required")
        Boolean enabled
) {
}
