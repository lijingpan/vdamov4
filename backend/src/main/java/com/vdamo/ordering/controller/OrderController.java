package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.OrderBatchCompleteRequest;
import com.vdamo.ordering.model.OrderBatchPaymentStatusUpdateRequest;
import com.vdamo.ordering.model.OrderBatchStatusUpdateRequest;
import com.vdamo.ordering.model.OrderDetailResponse;
import com.vdamo.ordering.model.OrderPaymentStatusUpdateRequest;
import com.vdamo.ordering.model.OrderStatusUpdateRequest;
import com.vdamo.ordering.model.OrderSummary;
import com.vdamo.ordering.service.OrderService;
import com.vdamo.ordering.service.PermissionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final MessageHelper messageHelper;
    private final OrderService orderService;
    private final PermissionService permissionService;

    public OrderController(
            MessageHelper messageHelper,
            OrderService orderService,
            PermissionService permissionService
    ) {
        this.messageHelper = messageHelper;
        this.orderService = orderService;
        this.permissionService = permissionService;
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

    @PatchMapping("/{orderId}/status")
    public ApiResponse<OrderDetailResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        permissionService.assertPermission("order:update-status");
        return ApiResponse.success(messageHelper.get("success.fetch"), orderService.updateOrderStatus(orderId, request));
    }

    @PatchMapping("/{orderId}/payment-status")
    public ApiResponse<OrderDetailResponse> updatePaymentStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderPaymentStatusUpdateRequest request
    ) {
        permissionService.assertPermission("order:update-payment");
        return ApiResponse.success(messageHelper.get("success.fetch"), orderService.updatePaymentStatus(orderId, request));
    }

    @PatchMapping("/{orderId}/complete")
    public ApiResponse<OrderDetailResponse> completeOrder(@PathVariable Long orderId) {
        permissionService.assertPermission("order:complete");
        return ApiResponse.success(messageHelper.get("success.fetch"), orderService.completeOrder(orderId));
    }

    @PatchMapping("/status")
    public ApiResponse<List<Long>> batchUpdateOrderStatus(@Valid @RequestBody OrderBatchStatusUpdateRequest request) {
        permissionService.assertPermission("order:update-status");
        return ApiResponse.success(messageHelper.get("success.fetch"), orderService.batchUpdateOrderStatus(request));
    }

    @PatchMapping("/payment-status")
    public ApiResponse<List<Long>> batchUpdatePaymentStatus(
            @Valid @RequestBody OrderBatchPaymentStatusUpdateRequest request
    ) {
        permissionService.assertPermission("order:update-payment");
        return ApiResponse.success(messageHelper.get("success.fetch"), orderService.batchUpdatePaymentStatus(request));
    }

    @PatchMapping("/complete")
    public ApiResponse<List<Long>> batchCompleteOrders(@Valid @RequestBody OrderBatchCompleteRequest request) {
        permissionService.assertPermission("order:complete");
        return ApiResponse.success(messageHelper.get("success.fetch"), orderService.batchCompleteOrders(request));
    }
}
