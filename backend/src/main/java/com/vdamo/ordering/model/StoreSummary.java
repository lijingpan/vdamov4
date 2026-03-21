package com.vdamo.ordering.model;

import java.util.List;
import java.math.BigDecimal;

public record StoreSummary(
        long id,
        String name,
        String countryCode,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String businessStatus,
        List<String> businessTypes
) {
}
