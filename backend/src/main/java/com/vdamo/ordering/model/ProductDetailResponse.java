package com.vdamo.ordering.model;

import java.util.List;

public record ProductDetailResponse(
        long id,
        long storeId,
        String storeName,
        String name,
        String code,
        String categoryCode,
        String productType,
        String specMode,
        String description,
        boolean attrEnabled,
        boolean materialEnabled,
        boolean weighedEnabled,
        boolean active,
        List<SpecItem> specs,
        List<SkuItem> skus,
        List<AttrItem> attrs
) {
    public record SpecItem(
            long id,
            String name,
            int sortOrder,
            List<SpecValueItem> values
    ) {
    }

    public record SpecValueItem(
            long id,
            String name,
            int sortOrder
    ) {
    }

    public record SkuItem(
            long id,
            String specKey,
            String specName,
            String skuCode,
            String barcode,
            int priceInCent,
            int linePriceInCent,
            int costPriceInCent,
            int boxFeeInCent,
            int stockQty,
            boolean autoReplenish,
            Integer weightUnitGram,
            int sortOrder,
            boolean active
    ) {
    }

    public record AttrItem(
            long id,
            String name,
            int sortOrder,
            List<AttrValueItem> values
    ) {
    }

    public record AttrValueItem(
            long id,
            String name,
            boolean isDefault,
            int sortOrder
    ) {
    }
}
