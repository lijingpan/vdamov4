package com.vdamo.ordering.model;

import java.time.LocalDateTime;

public record DiscountSummary(
        long id,
        long storeId,
        String storeName,
        String name,
        String code,
        String discountType,
        String amountType,
        int amountValue,
        int thresholdAmountInCent,
        boolean stackable,
        boolean enabled,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String remark
) {
}
