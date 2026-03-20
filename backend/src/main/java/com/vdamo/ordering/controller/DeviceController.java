package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.DeviceSummary;
import com.vdamo.ordering.service.DeviceService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final MessageHelper messageHelper;
    private final DeviceService deviceService;

    public DeviceController(MessageHelper messageHelper, DeviceService deviceService) {
        this.messageHelper = messageHelper;
        this.deviceService = deviceService;
    }

    @GetMapping
    public ApiResponse<List<DeviceSummary>> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(messageHelper.get("success.fetch"), deviceService.list(storeId, type, enabled, keyword));
    }
}
