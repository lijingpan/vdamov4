package com.vdamo.ordering.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TableUpsertRequest(
        @NotNull(message = "Store id is required")
        Long storeId,
        @NotBlank(message = "Area name is required")
        String areaName,
        @NotBlank(message = "Table name is required")
        String tableName,
        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be greater than 0")
        Integer capacity,
        @NotBlank(message = "Table status is required")
        String status
) {
}
