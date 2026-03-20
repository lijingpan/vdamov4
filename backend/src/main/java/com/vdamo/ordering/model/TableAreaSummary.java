package com.vdamo.ordering.model;

public record TableAreaSummary(
        long id,
        long storeId,
        String storeName,
        String areaName,
        String areaCode,
        int sortOrder,
        boolean enabled,
        int tableCount
) {
}
