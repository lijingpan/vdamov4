package com.vdamo.ordering.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ProductUpsertRequest(
        @NotNull(message = "Store id is required")
        Long storeId,
        @NotBlank(message = "Product name is required")
        String name,
        @NotBlank(message = "Product code is required")
        String code,
        @NotBlank(message = "Product category code is required")
        String categoryCode,
        String productType,
        String specMode,
        String description,
        Boolean attrEnabled,
        Boolean materialEnabled,
        Boolean weighedEnabled,
        @NotNull(message = "Product active flag is required")
        Boolean active,
        Integer priceInCent,
        List<RuleItem> rules,
        List<SpecItem> specs,
        List<SkuItem> skus,
        List<AttrItem> attrs
) {
    public List<RuleItem> rules() {
        return rules == null ? List.of() : rules;
    }

    public List<SpecItem> specs() {
        return specs == null ? List.of() : specs;
    }

    public List<SkuItem> skus() {
        return skus == null ? List.of() : skus;
    }

    public List<AttrItem> attrs() {
        return attrs == null ? List.of() : attrs;
    }

    public record SpecItem(
            String name,
            Integer sortOrder,
            List<SpecValueItem> values
    ) {
        public List<SpecValueItem> values() {
            return values == null ? List.of() : values;
        }
    }

    public record SpecValueItem(
            String name,
            Integer sortOrder
    ) {
    }

    public record RuleItem(
            String name,
            Integer sortOrder,
            List<RuleValueItem> values
    ) {
        public List<RuleValueItem> values() {
            return values == null ? List.of() : values;
        }
    }

    public record RuleValueItem(
            String name,
            Integer sortOrder
    ) {
    }

    public record SkuItem(
            String specKey,
            String specName,
            String skuCode,
            String barcode,
            Integer priceInCent,
            Integer linePriceInCent,
            Integer costPriceInCent,
            Integer boxFeeInCent,
            Integer stockQty,
            Boolean autoReplenish,
            Integer weightUnitGram,
            Integer sortOrder,
            Boolean active
    ) {
    }

    public record AttrItem(
            String name,
            Integer sortOrder,
            List<AttrValueItem> values
    ) {
        public List<AttrValueItem> values() {
            return values == null ? List.of() : values;
        }
    }

    public record AttrValueItem(
            String name,
            Boolean isDefault,
            Integer sortOrder
    ) {
    }
}
