package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.entity.OrderEntity;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.mapper.OrderMapper;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.model.SalesReportResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import org.springframework.stereotype.Service;

@Service
public class SalesReportService {

    private static final DateTimeFormatter EXPORT_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Set<String> OPEN_ORDER_STATUS = Set.of(
            "PENDING",
            "CONFIRMED",
            "IN_PROGRESS",
            "COOKING",
            "SERVED",
            "WAITING_CHECKOUT"
    );

    private final OrderMapper orderMapper;
    private final StoreMapper storeMapper;
    private final PermissionService permissionService;

    public SalesReportService(
            OrderMapper orderMapper,
            StoreMapper storeMapper,
            PermissionService permissionService
    ) {
        this.orderMapper = orderMapper;
        this.storeMapper = storeMapper;
        this.permissionService = permissionService;
    }

    public SalesReportResponse getReport(Long storeId, LocalDate startDate, LocalDate endDate) {
        permissionService.assertPermission("sales.report:view");
        return buildReport(storeId, startDate, endDate);
    }

    public byte[] exportReportCsv(Long storeId, LocalDate startDate, LocalDate endDate) {
        permissionService.assertPermission("sales.report:export");
        SalesReportResponse report = buildReport(storeId, startDate, endDate);
        StringBuilder builder = new StringBuilder(512);
        appendSummarySection(builder, report.summary());
        appendStoreSection(builder, report.storeRows());
        appendDailySection(builder, report.dailyTrend());
        // Add UTF-8 BOM so CSV opens correctly in spreadsheet tools.
        return ("\uFEFF" + builder).getBytes(StandardCharsets.UTF_8);
    }

    public String buildExportFileName(LocalDate startDate, LocalDate endDate) {
        String range = "all";
        if (startDate != null || endDate != null) {
            String from = startDate == null ? "start" : startDate.toString();
            String to = endDate == null ? "end" : endDate.toString();
            range = from + "_to_" + to;
        }
        return "sales-report-" + range + "-" + LocalDateTime.now().format(EXPORT_TIMESTAMP_FORMAT) + ".csv";
    }

    private SalesReportResponse buildReport(Long storeId, LocalDate startDate, LocalDate endDate) {
        List<Long> storeScope = permissionService.currentStoreIds();
        if (storeId != null) {
            permissionService.assertStoreAccess(storeId);
        }

        LambdaQueryWrapper<OrderEntity> wrapper = new LambdaQueryWrapper<OrderEntity>()
                .in(OrderEntity::getStoreId, storeScope)
                .orderByAsc(OrderEntity::getCreateTime)
                .orderByAsc(OrderEntity::getId);
        if (storeId != null) {
            wrapper.eq(OrderEntity::getStoreId, storeId);
        }
        if (startDate != null) {
            wrapper.ge(OrderEntity::getCreateTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(OrderEntity::getCreateTime, LocalDateTime.of(endDate, LocalTime.MAX));
        }

        List<OrderEntity> orders = orderMapper.selectList(wrapper);
        List<Long> reportStoreIds = storeId == null ? storeScope : List.of(storeId);
        Map<Long, String> storeNameMap = loadStoreNames(reportStoreIds);

        SummaryCounter summaryCounter = new SummaryCounter();
        Map<Long, SummaryCounter> storeCounterMap = initStoreCounterMap(reportStoreIds);
        Map<LocalDate, DailyCounter> dailyCounterMap = new TreeMap<>();

        for (OrderEntity order : orders) {
            long currentStoreId = order.getStoreId();
            SummaryCounter storeCounter = storeCounterMap.computeIfAbsent(currentStoreId, key -> new SummaryCounter());
            DailyCounter dailyCounter = dailyCounterMap.computeIfAbsent(
                    order.getCreateTime().toLocalDate(),
                    key -> new DailyCounter());

            summaryCounter.accept(order);
            storeCounter.accept(order);
            dailyCounter.accept(order);
        }

        List<SalesReportResponse.StoreRow> storeRows = new ArrayList<>();
        for (Map.Entry<Long, SummaryCounter> entry : storeCounterMap.entrySet()) {
            long currentStoreId = entry.getKey();
            SummaryCounter counter = entry.getValue();
            storeRows.add(new SalesReportResponse.StoreRow(
                    currentStoreId,
                    storeNameMap.getOrDefault(currentStoreId, ""),
                    counter.totalOrders,
                    counter.completedOrders,
                    counter.activeOrders,
                    counter.grossAmountInCent,
                    counter.paidAmountInCent,
                    counter.discountAmountInCent,
                    counter.appendOrderCount));
        }

        List<SalesReportResponse.DailyTrendRow> dailyTrend = dailyCounterMap.entrySet().stream()
                .map(entry -> new SalesReportResponse.DailyTrendRow(
                        entry.getKey().toString(),
                        entry.getValue().orderCount,
                        entry.getValue().completedOrderCount,
                        entry.getValue().activeOrderCount,
                        entry.getValue().grossAmountInCent,
                        entry.getValue().paidAmountInCent))
                .toList();

        return new SalesReportResponse(
                summaryCounter.toSummary(),
                storeRows,
                dailyTrend
        );
    }

    private void appendSummarySection(StringBuilder builder, SalesReportResponse.Summary summary) {
        builder.append("Summary").append('\n');
        builder.append("totalOrders,completedOrders,inProgressOrders,revenueInCent,paidInCent,discountInCent,appendOrderCount,averageOrderAmountInCent")
                .append('\n');
        builder.append(summary.totalOrders()).append(',')
                .append(summary.completedOrders()).append(',')
                .append(summary.activeOrders()).append(',')
                .append(summary.grossAmountInCent()).append(',')
                .append(summary.paidAmountInCent()).append(',')
                .append(summary.discountAmountInCent()).append(',')
                .append(summary.appendOrderCount()).append(',')
                .append(summary.averageOrderAmountInCent()).append('\n')
                .append('\n');
    }

    private void appendStoreSection(StringBuilder builder, List<SalesReportResponse.StoreRow> storeRows) {
        builder.append("By Store").append('\n');
        builder.append("storeId,storeName,totalOrders,completedOrders,inProgressOrders,revenueInCent,paidInCent,discountInCent,appendOrderCount,averageOrderAmountInCent")
                .append('\n');
        for (SalesReportResponse.StoreRow row : storeRows) {
            builder.append(row.storeId()).append(',')
                    .append(csvEscape(row.storeName())).append(',')
                    .append(row.orderCount()).append(',')
                    .append(row.completedOrderCount()).append(',')
                    .append(row.activeOrderCount()).append(',')
                    .append(row.grossAmountInCent()).append(',')
                    .append(row.paidAmountInCent()).append(',')
                    .append(row.discountAmountInCent()).append(',')
                    .append(row.appendOrderCount()).append(',')
                    .append(row.orderCount() == 0 ? 0 : row.grossAmountInCent() / row.orderCount())
                    .append('\n');
        }
        builder.append('\n');
    }

    private void appendDailySection(StringBuilder builder, List<SalesReportResponse.DailyTrendRow> dailyRows) {
        builder.append("By Date").append('\n');
        builder.append("date,totalOrders,completedOrders,inProgressOrders,revenueInCent,paidInCent")
                .append('\n');
        for (SalesReportResponse.DailyTrendRow row : dailyRows) {
            builder.append(csvEscape(row.businessDate())).append(',')
                    .append(row.orderCount()).append(',')
                    .append(row.completedOrderCount()).append(',')
                    .append(row.activeOrderCount()).append(',')
                    .append(row.grossAmountInCent()).append(',')
                    .append(row.paidAmountInCent())
                    .append('\n');
        }
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuote = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        if (!needsQuote) {
            return escaped;
        }
        StringJoiner joiner = new StringJoiner("", "\"", "\"");
        joiner.add(escaped);
        return joiner.toString();
    }

    private Map<Long, SummaryCounter> initStoreCounterMap(List<Long> storeIds) {
        Map<Long, SummaryCounter> map = new LinkedHashMap<>();
        for (Long storeId : storeIds) {
            map.put(storeId, new SummaryCounter());
        }
        return map;
    }

    private Map<Long, String> loadStoreNames(List<Long> storeIds) {
        if (storeIds.isEmpty()) {
            return Map.of();
        }
        return storeMapper.selectList(new LambdaQueryWrapper<StoreEntity>().in(StoreEntity::getId, storeIds))
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        StoreEntity::getId,
                        StoreEntity::getName,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private static int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private static final class SummaryCounter {
        private int totalOrders;
        private int completedOrders;
        private int activeOrders;
        private int grossAmountInCent;
        private int paidAmountInCent;
        private int discountAmountInCent;
        private int appendOrderCount;

        private void accept(OrderEntity order) {
            totalOrders++;
            if ("COMPLETED".equals(order.getOrderStatus())) {
                completedOrders++;
            }
            if (OPEN_ORDER_STATUS.contains(order.getOrderStatus())) {
                activeOrders++;
            }
            if (defaultInt(order.getAppendCount()) > 0) {
                appendOrderCount++;
            }
            grossAmountInCent += defaultInt(order.getPayableAmountInCent());
            paidAmountInCent += defaultInt(order.getPaidAmountInCent());
            discountAmountInCent += defaultInt(order.getDiscountAmountInCent());
        }

        private SalesReportResponse.Summary toSummary() {
            return new SalesReportResponse.Summary(
                    totalOrders,
                    completedOrders,
                    activeOrders,
                    grossAmountInCent,
                    paidAmountInCent,
                    discountAmountInCent,
                    appendOrderCount,
                    totalOrders == 0 ? 0 : grossAmountInCent / totalOrders
            );
        }
    }

    private static final class DailyCounter {
        private int orderCount;
        private int completedOrderCount;
        private int activeOrderCount;
        private int grossAmountInCent;
        private int paidAmountInCent;

        private void accept(OrderEntity order) {
            orderCount++;
            if ("COMPLETED".equals(order.getOrderStatus())) {
                completedOrderCount++;
            }
            if (OPEN_ORDER_STATUS.contains(order.getOrderStatus())) {
                activeOrderCount++;
            }
            grossAmountInCent += defaultInt(order.getPayableAmountInCent());
            paidAmountInCent += defaultInt(order.getPaidAmountInCent());
        }
    }
}
