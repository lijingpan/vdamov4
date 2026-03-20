package com.vdamo.ordering.model;

public record ProductSummary(
        long id,
        String name,
        String code,
        String categoryCode,
        int priceInCent,
        boolean active
) {
}
