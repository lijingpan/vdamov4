package com.vdamo.ordering.model;

import java.util.Map;

public record DashboardSummaryResponse(
        int totalStores,
        int activeProducts,
        int totalMembers,
        int totalDevices,
        int totalTables,
        int activeOrders,
        int todayOrders,
        Map<String, Integer> tableStatus
) {
}
