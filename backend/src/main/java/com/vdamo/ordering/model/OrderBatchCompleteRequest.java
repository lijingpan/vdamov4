package com.vdamo.ordering.model;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderBatchCompleteRequest(
        @NotEmpty(message = "Order ids are required")
        List<Long> orderIds
) {
}
