package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.RoleSummary;
import com.vdamo.ordering.service.RoleService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final MessageHelper messageHelper;
    private final RoleService roleService;

    public RoleController(MessageHelper messageHelper, RoleService roleService) {
        this.messageHelper = messageHelper;
        this.roleService = roleService;
    }

    @GetMapping
    public ApiResponse<List<RoleSummary>> list() {
        return ApiResponse.success(messageHelper.get("success.fetch"), roleService.listAll());
    }
}

