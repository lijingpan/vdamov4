package com.vdamo.ordering.model;

import jakarta.validation.constraints.NotBlank;

public record OrderStatusUpdateRequest(
        @NotBlank(message = "Order status is required")
        String orderStatus
) {
}
