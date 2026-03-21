package com.vdamo.ordering.model;

public record ProductSummary(
        long id,
        long storeId,
        String storeName,
        String name,
        String code,
        String categoryCode,
        String productType,
        String specMode,
        int priceInCent,
        int maxPriceInCent,
        int skuCount,
        boolean attrEnabled,
        boolean weighedEnabled,
        boolean active
) {
}
