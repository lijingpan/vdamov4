package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.MemberUpsertRequest;
import com.vdamo.ordering.model.MemberSummary;
import com.vdamo.ordering.service.MemberService;
import com.vdamo.ordering.service.PermissionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MessageHelper messageHelper;
    private final MemberService memberService;
    private final PermissionService permissionService;

    public MemberController(
            MessageHelper messageHelper,
            MemberService memberService,
            PermissionService permissionService
    ) {
        this.messageHelper = messageHelper;
        this.memberService = memberService;
        this.permissionService = permissionService;
    }

    @GetMapping
    public ApiResponse<List<MemberSummary>> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String levelCode,
            @RequestParam(required = false) String keyword
    ) {
        permissionService.assertPermission("member:view");
        return ApiResponse.success(
                messageHelper.get("success.fetch"),
                memberService.list(storeId, countryCode, levelCode, keyword));
    }

    @PostMapping
    public ApiResponse<MemberSummary> create(@Valid @RequestBody MemberUpsertRequest request) {
        permissionService.assertPermission("member:create");
        return ApiResponse.success(messageHelper.get("success.fetch"), memberService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<MemberSummary> update(
            @PathVariable Long id,
            @Valid @RequestBody MemberUpsertRequest request
    ) {
        permissionService.assertPermission("member:update");
        return ApiResponse.success(messageHelper.get("success.fetch"), memberService.update(id, request));
    }

    @GetMapping("/by-phone")
    public ApiResponse<MemberSummary> findByPhone(@RequestParam @NotBlank(message = "{error.phone.required}") String phoneE164) {
        permissionService.assertPermission("member:view");
        return ApiResponse.success(messageHelper.get("success.fetch"), memberService.findByPhone(phoneE164));
    }
}
