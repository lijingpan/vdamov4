package com.vdamo.ordering.model;

import java.util.List;

public record AuthLoginResponse(
        String accessToken,
        AuthenticatedUser user,
        List<MenuSummary> menus
) {
}

