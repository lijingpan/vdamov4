package com.vdamo.ordering.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderBatchPaymentStatusUpdateRequest(
        @NotEmpty(message = "Order ids are required")
        List<Long> orderIds,
        @NotBlank(message = "Payment status is required")
        String paymentStatus
) {
}
