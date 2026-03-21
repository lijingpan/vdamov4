package com.vdamo.ordering.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductUpsertRequest(
        @NotNull(message = "Store id is required")
        Long storeId,
        @NotBlank(message = "Product name is required")
        String name,
        @NotBlank(message = "Product code is required")
        String code,
        @NotBlank(message = "Product category code is required")
        String categoryCode,
        @NotNull(message = "Product price is required")
        @Min(value = 0, message = "Product price must be greater than or equal to 0")
        Integer priceInCent,
        @NotNull(message = "Product active flag is required")
        Boolean active
) {
}
