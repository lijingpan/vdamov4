package com.vdamo.ordering.model;

public record ProductSummary(
        long id,
        long storeId,
        String storeName,
        String name,
        String code,
        String categoryCode,
        int priceInCent,
        boolean active
) {
}
