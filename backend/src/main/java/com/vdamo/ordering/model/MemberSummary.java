package com.vdamo.ordering.model;

public record MemberSummary(
        long id,
        long storeId,
        String storeName,
        String levelCode,
        String displayName,
        String countryCode,
        String phoneNational,
        String phoneE164
) {
}
