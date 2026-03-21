package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.ProductDetailResponse;
import com.vdamo.ordering.model.ProductStatusUpdateRequest;
import com.vdamo.ordering.model.ProductSummary;
import com.vdamo.ordering.model.ProductUpsertRequest;
import com.vdamo.ordering.service.PermissionService;
import com.vdamo.ordering.service.ProductService;
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
@RequestMapping("/api/v1/products")
public class ProductController {

    private final MessageHelper messageHelper;
    private final ProductService productService;
    private final PermissionService permissionService;

    public ProductController(
            MessageHelper messageHelper,
            ProductService productService,
            PermissionService permissionService
    ) {
        this.messageHelper = messageHelper;
        this.productService = productService;
        this.permissionService = permissionService;
    }

    @GetMapping
    public ApiResponse<List<ProductSummary>> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String keyword
    ) {
        permissionService.assertPermission("product:view");
        return ApiResponse.success(
                messageHelper.get("success.fetch"),
                productService.list(storeId, active, categoryCode, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> detail(@PathVariable Long id) {
        permissionService.assertPermission("product:view");
        return ApiResponse.success(messageHelper.get("success.fetch"), productService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<ProductSummary> create(@Valid @RequestBody ProductUpsertRequest request) {
        permissionService.assertPermission("product:create");
        return ApiResponse.success(messageHelper.get("success.fetch"), productService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductSummary> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpsertRequest request
    ) {
        permissionService.assertPermission("product:update");
        return ApiResponse.success(messageHelper.get("success.fetch"), productService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<ProductSummary> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ProductStatusUpdateRequest request
    ) {
        permissionService.assertPermission("product:status");
        return ApiResponse.success(messageHelper.get("success.fetch"), productService.updateStatus(id, request));
    }
}
