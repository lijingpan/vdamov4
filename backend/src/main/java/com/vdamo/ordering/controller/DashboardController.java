package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.DashboardSummaryResponse;
import com.vdamo.ordering.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final MessageHelper messageHelper;
    private final DashboardService dashboardService;

    public DashboardController(MessageHelper messageHelper, DashboardService dashboardService) {
        this.messageHelper = messageHelper;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> summary() {
        return ApiResponse.success(messageHelper.get("success.fetch"), dashboardService.getSummary());
    }
}
