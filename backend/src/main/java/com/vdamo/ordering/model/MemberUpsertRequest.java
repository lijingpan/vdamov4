package com.vdamo.ordering.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberUpsertRequest(
        @NotNull(message = "Store id is required")
        Long storeId,
        @NotBlank(message = "Member level is required")
        String levelCode,
        @NotBlank(message = "Member display name is required")
        String displayName,
        @NotBlank(message = "Country code is required")
        String countryCode,
        @NotBlank(message = "Phone national is required")
        String phoneNational,
        @NotBlank(message = "Phone E164 is required")
        String phoneE164
) {
}
