package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.OrderDetailResponse;
import com.vdamo.ordering.model.OrderSummary;
import com.vdamo.ordering.service.OrderService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final MessageHelper messageHelper;
    private final OrderService orderService;

    public OrderController(MessageHelper messageHelper, OrderService orderService) {
        this.messageHelper = messageHelper;
        this.orderService = orderService;
    }

    @GetMapping
    public ApiResponse<List<OrderSummary>> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String paymentStatus
    ) {
        return ApiResponse.success(
                messageHelper.get("success.fetch"),
                orderService.list(storeId, keyword, orderStatus, paymentStatus));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponse> detail(@PathVariable Long orderId) {
        return ApiResponse.success(messageHelper.get("success.fetch"), orderService.getDetail(orderId));
    }
}
