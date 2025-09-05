package com.knockbook.backend.dto;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EmailVerificationTokenResponse {
    private String emailVerificationToken;
}
