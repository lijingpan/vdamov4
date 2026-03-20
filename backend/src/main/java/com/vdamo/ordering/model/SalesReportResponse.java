package com.vdamo.ordering.model;

import java.util.List;

public record SalesReportResponse(
        Summary summary,
        List<StoreRow> storeRows,
        List<DailyTrendRow> dailyTrend
) {

    public record Summary(
            int totalOrders,
            int completedOrders,
            int activeOrders,
            int grossAmountInCent,
            int paidAmountInCent,
            int discountAmountInCent,
            int appendOrderCount,
            int averageOrderAmountInCent
    ) {
    }

    public record StoreRow(
            long storeId,
            String storeName,
            int orderCount,
            int completedOrderCount,
            int activeOrderCount,
            int grossAmountInCent,
            int paidAmountInCent,
            int discountAmountInCent,
            int appendOrderCount
    ) {
    }

    public record DailyTrendRow(
            String businessDate,
            int orderCount,
            int completedOrderCount,
            int activeOrderCount,
            int grossAmountInCent,
            int paidAmountInCent
    ) {
    }
}
