package com.vdamo.ordering.model;

public record TableCurrentOrderSummary(
        long orderId,
        String orderNo,
        String orderStatus,
        String paymentStatus,
        int payableAmountInCent,
        boolean hasAppend
) {
}
