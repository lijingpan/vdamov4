package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.MemberSummary;
import com.vdamo.ordering.service.MemberService;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MessageHelper messageHelper;
    private final MemberService memberService;

    public MemberController(MessageHelper messageHelper, MemberService memberService) {
        this.messageHelper = messageHelper;
        this.memberService = memberService;
    }

    @GetMapping
    public ApiResponse<List<MemberSummary>> list() {
        return ApiResponse.success(messageHelper.get("success.fetch"), memberService.list());
    }

    @GetMapping("/by-phone")
    public ApiResponse<MemberSummary> findByPhone(@RequestParam @NotBlank(message = "{error.phone.required}") String phoneE164) {
        return ApiResponse.success(messageHelper.get("success.fetch"), memberService.findByPhone(phoneE164));
    }
}
