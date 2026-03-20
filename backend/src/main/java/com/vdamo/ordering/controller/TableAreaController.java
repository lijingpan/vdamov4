package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.TableAreaSummary;
import com.vdamo.ordering.service.TableAreaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
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
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(
                messageHelper.get("success.fetch"),
                tableAreaService.list(storeId, keyword));
    }
}
