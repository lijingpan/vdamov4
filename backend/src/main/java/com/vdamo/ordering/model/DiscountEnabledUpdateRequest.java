package com.vdamo.ordering.model;

import jakarta.validation.constraints.NotNull;

public record DiscountEnabledUpdateRequest(
        @NotNull(message = "Discount enabled flag is required")
        Boolean enabled
) {
}
