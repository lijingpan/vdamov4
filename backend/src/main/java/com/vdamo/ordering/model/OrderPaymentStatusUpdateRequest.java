package com.vdamo.ordering.model;

import jakarta.validation.constraints.NotBlank;

public record OrderPaymentStatusUpdateRequest(
        @NotBlank(message = "Payment status is required")
        String paymentStatus
) {
}
