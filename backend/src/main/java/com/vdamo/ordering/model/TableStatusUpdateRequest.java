package com.vdamo.ordering.model;

import jakarta.validation.constraints.NotBlank;

public record TableStatusUpdateRequest(
        @NotBlank(message = "Table status is required")
        String status
) {
}
