package com.knockbook.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ApplyCouponRequest {
    @NonNull @NotBlank
    private String couponIssuanceId;
    private String code;
}

