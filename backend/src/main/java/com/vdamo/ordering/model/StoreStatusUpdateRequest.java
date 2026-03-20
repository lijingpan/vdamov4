package com.vdamo.ordering.model;

import jakarta.validation.constraints.NotBlank;

public record StoreStatusUpdateRequest(
        @NotBlank(message = "Business status is required")
        String businessStatus
) {
}
