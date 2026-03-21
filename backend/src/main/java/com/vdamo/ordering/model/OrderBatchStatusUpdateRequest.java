package com.vdamo.ordering.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderBatchStatusUpdateRequest(
        @NotEmpty(message = "Order ids are required")
        List<Long> orderIds,
        @NotBlank(message = "Order status is required")
        String orderStatus
) {
}
