package com.vdamo.ordering.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record DiscountUpsertRequest(
        @NotNull(message = "Store id is required")
        Long storeId,
        @NotBlank(message = "Discount name is required")
        String name,
        @NotBlank(message = "Discount code is required")
        String code,
        @NotBlank(message = "Discount type is required")
        String discountType,
        @NotBlank(message = "Amount type is required")
        String amountType,
        @NotNull(message = "Amount value is required")
        @Min(value = 1, message = "Amount value must be greater than 0")
        Integer amountValue,
        @NotNull(message = "Threshold amount is required")
        @Min(value = 0, message = "Threshold amount must be greater than or equal to 0")
        Integer thresholdAmountInCent,
        @NotNull(message = "Stackable flag is required")
        Boolean stackable,
        @NotNull(message = "Enabled flag is required")
        Boolean enabled,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String remark
) {
}
