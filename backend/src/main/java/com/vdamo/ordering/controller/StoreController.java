package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.StoreSummary;
import com.vdamo.ordering.service.StoreService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final MessageHelper messageHelper;
    private final StoreService storeService;

    public StoreController(MessageHelper messageHelper, StoreService storeService) {
        this.messageHelper = messageHelper;
        this.storeService = storeService;
    }

    @GetMapping
    public ApiResponse<List<StoreSummary>> list() {
        return ApiResponse.success(messageHelper.get("success.fetch"), storeService.list());
    }
}
