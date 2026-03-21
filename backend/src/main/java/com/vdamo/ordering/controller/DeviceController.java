package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.DeviceEnabledUpdateRequest;
import com.vdamo.ordering.model.DeviceSummary;
import com.vdamo.ordering.model.DeviceUpsertRequest;
import com.vdamo.ordering.service.PermissionService;
import com.vdamo.ordering.service.DeviceService;
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
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final MessageHelper messageHelper;
    private final DeviceService deviceService;
    private final PermissionService permissionService;

    public DeviceController(
            MessageHelper messageHelper,
            DeviceService deviceService,
            PermissionService permissionService
    ) {
        this.messageHelper = messageHelper;
        this.deviceService = deviceService;
        this.permissionService = permissionService;
    }

    @GetMapping
    public ApiResponse<List<DeviceSummary>> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String keyword
    ) {
        permissionService.assertPermission("device:view");
        return ApiResponse.success(messageHelper.get("success.fetch"), deviceService.list(storeId, type, enabled, keyword));
    }

    @PostMapping
    public ApiResponse<DeviceSummary> create(@Valid @RequestBody DeviceUpsertRequest request) {
        permissionService.assertPermission("device:create");
        return ApiResponse.success(messageHelper.get("success.fetch"), deviceService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DeviceSummary> update(
            @PathVariable Long id,
            @Valid @RequestBody DeviceUpsertRequest request
    ) {
        permissionService.assertPermission("device:update");
        return ApiResponse.success(messageHelper.get("success.fetch"), deviceService.update(id, request));
    }

    @PatchMapping("/{id}/enabled")
    public ApiResponse<DeviceSummary> updateEnabled(
            @PathVariable Long id,
            @Valid @RequestBody DeviceEnabledUpdateRequest request
    ) {
        permissionService.assertPermission("device:enable");
        return ApiResponse.success(messageHelper.get("success.fetch"), deviceService.updateEnabled(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        permissionService.assertPermission("device:delete");
        deviceService.delete(id);
        return ApiResponse.success(messageHelper.get("success.fetch"), null);
    }
}
