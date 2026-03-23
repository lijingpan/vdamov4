package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.DiscountEnabledUpdateRequest;
import com.vdamo.ordering.model.DiscountSummary;
import com.vdamo.ordering.model.DiscountUpsertRequest;
import com.vdamo.ordering.service.DiscountService;
import com.vdamo.ordering.service.PermissionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/discounts")
public class DiscountController {

    private final MessageHelper messageHelper;
    private final DiscountService discountService;
    private final PermissionService permissionService;

    public DiscountController(
            MessageHelper messageHelper,
            DiscountService discountService,
            PermissionService permissionService
    ) {
        this.messageHelper = messageHelper;
        this.discountService = discountService;
        this.permissionService = permissionService;
    }

    @GetMapping
    public ApiResponse<List<DiscountSummary>> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String discountType,
            @RequestParam(required = false) String keyword
    ) {
        permissionService.assertPermission("discount:view");
        return ApiResponse.success(
                messageHelper.get("success.fetch"),
                discountService.list(storeId, enabled, discountType, keyword));
    }

    @PostMapping
    public ApiResponse<DiscountSummary> create(@Valid @RequestBody DiscountUpsertRequest request) {
        permissionService.assertPermission("discount:create");
        return ApiResponse.success(messageHelper.get("success.fetch"), discountService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DiscountSummary> update(
            @PathVariable Long id,
            @Valid @RequestBody DiscountUpsertRequest request
    ) {
        permissionService.assertPermission("discount:update");
        return ApiResponse.success(messageHelper.get("success.fetch"), discountService.update(id, request));
    }

    @PatchMapping("/{id}/enabled")
    public ApiResponse<DiscountSummary> updateEnabled(
            @PathVariable Long id,
            @Valid @RequestBody DiscountEnabledUpdateRequest request
    ) {
        permissionService.assertPermission("discount:enable");
        return ApiResponse.success(messageHelper.get("success.fetch"), discountService.updateEnabled(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        permissionService.assertPermission("discount:delete");
        discountService.delete(id);
        return ApiResponse.success(messageHelper.get("success.fetch"), null);
    }
}
