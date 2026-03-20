package com.vdamo.ordering.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TableAreaUpsertRequest(
        @NotNull(message = "Store id is required")
        Long storeId,
        @NotBlank(message = "Area name is required")
        String areaName,
        @NotBlank(message = "Area code is required")
        String areaCode,
        @NotNull(message = "Sort order is required")
        @Min(value = 0, message = "Sort order must be greater than or equal to 0")
        Integer sortOrder,
        @NotNull(message = "Enabled flag is required")
        Boolean enabled
) {
}
