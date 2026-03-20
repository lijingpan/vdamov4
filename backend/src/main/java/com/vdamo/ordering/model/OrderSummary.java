package com.vdamo.ordering.model;

import java.time.LocalDateTime;

public record OrderSummary(
        long id,
        String orderNo,
        long storeId,
        String storeName,
        long tableId,
        String tableName,
        Long memberId,
        String memberDisplayName,
        int originalAmountInCent,
        int discountAmountInCent,
        int payableAmountInCent,
        int paidAmountInCent,
        String orderStatus,
        String paymentStatus,
        boolean hasAppend,
        LocalDateTime createTime
) {
}
