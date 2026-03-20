package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.TableAreaEnabledUpdateRequest;
import com.vdamo.ordering.model.TableAreaSummary;
import com.vdamo.ordering.model.TableAreaUpsertRequest;
import com.vdamo.ordering.service.TableAreaService;
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
@RequestMapping("/api/v1/table-areas")
public class TableAreaController {

    private final MessageHelper messageHelper;
    private final TableAreaService tableAreaService;

    public TableAreaController(MessageHelper messageHelper, TableAreaService tableAreaService) {
        this.messageHelper = messageHelper;
        this.tableAreaService = tableAreaService;
    }

    @GetMapping
    public ApiResponse<List<TableAreaSummary>> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled
    ) {
        return ApiResponse.success(
                messageHelper.get("success.fetch"),
                tableAreaService.list(storeId, keyword, enabled));
    }

    @PostMapping
    public ApiResponse<TableAreaSummary> create(@Valid @RequestBody TableAreaUpsertRequest request) {
        return ApiResponse.success(messageHelper.get("success.fetch"), tableAreaService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TableAreaSummary> update(
            @PathVariable Long id,
            @Valid @RequestBody TableAreaUpsertRequest request
    ) {
        return ApiResponse.success(messageHelper.get("success.fetch"), tableAreaService.update(id, request));
    }

    @PatchMapping("/{id}/enabled")
    public ApiResponse<TableAreaSummary> updateEnabled(
            @PathVariable Long id,
            @Valid @RequestBody TableAreaEnabledUpdateRequest request
    ) {
        return ApiResponse.success(messageHelper.get("success.fetch"), tableAreaService.updateEnabled(id, request));
    }
}
