package com.vdamo.ordering.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record StoreUpsertRequest(
        @NotBlank(message = "Store name is required")
        String name,
        @NotBlank(message = "Country code is required")
        String countryCode,
        @NotBlank(message = "Store address is required")
        @Size(max = 255, message = "Address length must be <= 255")
        String address,
        @NotNull(message = "Store latitude is required")
        BigDecimal latitude,
        @NotNull(message = "Store longitude is required")
        BigDecimal longitude,
        @NotBlank(message = "Business status is required")
        String businessStatus,
        @NotEmpty(message = "At least one business type is required")
        List<String> businessTypes
) {
}
