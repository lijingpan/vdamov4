package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.TableStatusUpdateRequest;
import com.vdamo.ordering.model.TableSummary;
import com.vdamo.ordering.model.TableUpsertRequest;
import com.vdamo.ordering.service.TableService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tables")
public class TableController {

    private final MessageHelper messageHelper;
    private final TableService tableService;

    public TableController(MessageHelper messageHelper, TableService tableService) {
        this.messageHelper = messageHelper;
        this.tableService = tableService;
    }

    @GetMapping
    public ApiResponse<List<TableSummary>> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(messageHelper.get("success.fetch"), tableService.list(storeId, status, keyword));
    }

    @PostMapping
    public ApiResponse<TableSummary> create(@Valid @RequestBody TableUpsertRequest request) {
        return ApiResponse.success(messageHelper.get("success.fetch"), tableService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TableSummary> update(
            @PathVariable Long id,
            @Valid @RequestBody TableUpsertRequest request
    ) {
        return ApiResponse.success(messageHelper.get("success.fetch"), tableService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<TableSummary> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody TableStatusUpdateRequest request
    ) {
        return ApiResponse.success(messageHelper.get("success.fetch"), tableService.updateStatus(id, request));
    }
}
