package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.ProductCategoryEnabledUpdateRequest;
import com.vdamo.ordering.model.ProductCategorySummary;
import com.vdamo.ordering.model.ProductCategoryUpsertRequest;
import com.vdamo.ordering.service.PermissionService;
import com.vdamo.ordering.service.ProductCategoryService;
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
@RequestMapping("/api/v1/product-categories")
public class ProductCategoryController {

    private final MessageHelper messageHelper;
    private final ProductCategoryService productCategoryService;
    private final PermissionService permissionService;

    public ProductCategoryController(
            MessageHelper messageHelper,
            ProductCategoryService productCategoryService,
            PermissionService permissionService
    ) {
        this.messageHelper = messageHelper;
        this.productCategoryService = productCategoryService;
        this.permissionService = permissionService;
    }

    @GetMapping
    public ApiResponse<List<ProductCategorySummary>> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled
    ) {
        permissionService.assertPermission("product.category:view");
        return ApiResponse.success(
                messageHelper.get("success.fetch"),
                productCategoryService.list(storeId, keyword, enabled));
    }

    @PostMapping
    public ApiResponse<ProductCategorySummary> create(@Valid @RequestBody ProductCategoryUpsertRequest request) {
        permissionService.assertPermission("product.category:create");
        return ApiResponse.success(messageHelper.get("success.fetch"), productCategoryService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductCategorySummary> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductCategoryUpsertRequest request
    ) {
        permissionService.assertPermission("product.category:update");
        return ApiResponse.success(messageHelper.get("success.fetch"), productCategoryService.update(id, request));
    }

    @PatchMapping("/{id}/enabled")
    public ApiResponse<ProductCategorySummary> updateEnabled(
            @PathVariable Long id,
            @Valid @RequestBody ProductCategoryEnabledUpdateRequest request
    ) {
        permissionService.assertPermission("product.category:enable");
        return ApiResponse.success(messageHelper.get("success.fetch"), productCategoryService.updateEnabled(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        permissionService.assertPermission("product.category:delete");
        productCategoryService.delete(id);
        return ApiResponse.success(messageHelper.get("success.fetch"), null);
    }
}
