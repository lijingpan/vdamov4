package com.vdamo.ordering.model;

public record ProductCategorySummary(
        long id,
        long storeId,
        String storeName,
        String categoryName,
        String categoryCode,
        int sortOrder,
        boolean enabled,
        int productCount
) {
}
