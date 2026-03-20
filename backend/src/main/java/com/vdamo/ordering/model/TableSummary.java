package com.vdamo.ordering.model;

public record TableSummary(
        long id,
        long storeId,
        String storeName,
        String areaName,
        String tableCode,
        String tableName,
        int capacity,
        String status,
        TableCurrentOrderSummary currentOrder
) {
}
