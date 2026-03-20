package com.vdamo.ordering.model;

import java.util.List;

public record StoreSummary(
        long id,
        String name,
        String countryCode,
        String businessStatus,
        List<String> businessTypes
) {
}
