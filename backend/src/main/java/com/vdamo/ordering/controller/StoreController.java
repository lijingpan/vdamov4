package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.StoreStatusUpdateRequest;
import com.vdamo.ordering.model.StoreSummary;
import com.vdamo.ordering.model.StoreUpsertRequest;
import com.vdamo.ordering.service.StoreService;
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
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final MessageHelper messageHelper;
    private final StoreService storeService;

    public StoreController(MessageHelper messageHelper, StoreService storeService) {
        this.messageHelper = messageHelper;
        this.storeService = storeService;
    }

    @GetMapping
    public ApiResponse<List<StoreSummary>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.success(messageHelper.get("success.fetch"), storeService.list(keyword, status));
    }

    @PostMapping
    public ApiResponse<StoreSummary> create(@Valid @RequestBody StoreUpsertRequest request) {
        return ApiResponse.success(messageHelper.get("success.fetch"), storeService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<StoreSummary> update(
            @PathVariable Long id,
            @Valid @RequestBody StoreUpsertRequest request
    ) {
        return ApiResponse.success(messageHelper.get("success.fetch"), storeService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<StoreSummary> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StoreStatusUpdateRequest request
    ) {
        return ApiResponse.success(messageHelper.get("success.fetch"), storeService.updateStatus(id, request));
    }
}
