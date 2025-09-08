package com.knockbook.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class VerifyEmailRequest {
    @NotBlank
    private String emailVerificationToken;
    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$")
    private String code;
}
