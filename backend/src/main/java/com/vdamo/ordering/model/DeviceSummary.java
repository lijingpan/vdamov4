package com.vdamo.ordering.model;

public record DeviceSummary(
        long id,
        long storeId,
        String name,
        String type,
        String brand,
        String sn,
        String size,
        boolean enabled
) {
}
