package com.vdamo.ordering.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record StoreUpsertRequest(
        @NotBlank(message = "Store name is required")
        String name,
        @NotBlank(message = "Country code is required")
        String countryCode,
        @NotBlank(message = "Business status is required")
        String businessStatus,
        @NotEmpty(message = "At least one business type is required")
        List<String> businessTypes
) {
}
