package com.vdamo.ordering.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductCategoryUpsertRequest(
        @NotNull(message = "Store id is required")
        Long storeId,
        @NotBlank(message = "Category name is required")
        String categoryName,
        @NotBlank(message = "Category code is required")
        String categoryCode,
        @NotNull(message = "Sort order is required")
        @Min(value = 0, message = "Sort order must be greater than or equal to 0")
        Integer sortOrder,
        @NotNull(message = "Category enabled flag is required")
        Boolean enabled
) {
}
