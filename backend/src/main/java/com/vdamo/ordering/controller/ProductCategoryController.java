package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.ProductCategorySummary;
import com.vdamo.ordering.service.ProductCategoryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/product-categories")
public class ProductCategoryController {

    private final MessageHelper messageHelper;
    private final ProductCategoryService productCategoryService;

    public ProductCategoryController(MessageHelper messageHelper, ProductCategoryService productCategoryService) {
        this.messageHelper = messageHelper;
        this.productCategoryService = productCategoryService;
    }

    @GetMapping
    public ApiResponse<List<ProductCategorySummary>> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled
    ) {
        return ApiResponse.success(
                messageHelper.get("success.fetch"),
                productCategoryService.list(storeId, keyword, enabled));
    }
}
