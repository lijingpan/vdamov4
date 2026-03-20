package com.vdamo.ordering.model;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(
        @NotBlank(message = "{error.auth.username.required}") String username,
        @NotBlank(message = "{error.auth.password.required}") String password
) {
}

