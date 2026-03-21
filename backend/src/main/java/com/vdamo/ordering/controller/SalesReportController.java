package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.SalesReportResponse;
import com.vdamo.ordering.service.PermissionService;
import com.vdamo.ordering.service.SalesReportService;
import java.time.LocalDate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class SalesReportController {

    private final MessageHelper messageHelper;
    private final SalesReportService salesReportService;
    private final PermissionService permissionService;

    public SalesReportController(
            MessageHelper messageHelper,
            SalesReportService salesReportService,
            PermissionService permissionService
    ) {
        this.messageHelper = messageHelper;
        this.salesReportService = salesReportService;
        this.permissionService = permissionService;
    }

    @GetMapping("/sales")
    public ApiResponse<SalesReportResponse> sales(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        permissionService.assertPermission("sales.report:view");
        return ApiResponse.success(
                messageHelper.get("success.fetch"),
                salesReportService.getReport(storeId, startDate, endDate));
    }

    @GetMapping("/sales/export")
    public ResponseEntity<byte[]> exportSales(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        permissionService.assertPermission("sales.report:export");
        byte[] body = salesReportService.exportReportCsv(storeId, startDate, endDate);
        String fileName = salesReportService.buildExportFileName(startDate, endDate);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(body);
    }
}
