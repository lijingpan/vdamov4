package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.ProductSummary;
import com.vdamo.ordering.service.ProductService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final MessageHelper messageHelper;
    private final ProductService productService;

    public ProductController(MessageHelper messageHelper, ProductService productService) {
        this.messageHelper = messageHelper;
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<List<ProductSummary>> list() {
        return ApiResponse.success(messageHelper.get("success.fetch"), productService.list());
    }
}
