package com.vdamo.ordering.controller;

import com.vdamo.ordering.common.api.ApiResponse;
import com.vdamo.ordering.common.i18n.MessageHelper;
import com.vdamo.ordering.model.UserSummary;
import com.vdamo.ordering.service.UserService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final MessageHelper messageHelper;
    private final UserService userService;

    public UserController(MessageHelper messageHelper, UserService userService) {
        this.messageHelper = messageHelper;
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<UserSummary>> list() {
        return ApiResponse.success(messageHelper.get("success.fetch"), userService.listAll());
    }
}
