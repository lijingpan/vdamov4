package com.vdamo.ordering.model;

public record DeviceSummary(
        long id,
        long storeId,
        String storeName,
        String name,
        String type,
        String purpose,
        String brand,
        String sn,
        String size,
        String onlineStatus,
        boolean enabled
) {
}
