package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.TableSummary;
import com.vdamo.ordering.service.TableService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ApiResponse<List<TableSummary>> list(@RequestParam(required = false) Long storeId) {
        return ApiResponse.success(messageHelper.get("success.fetch"), tableService.list(storeId));
    }
}
