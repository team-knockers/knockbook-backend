package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Identity {
    private Long id;
    private Long userId;
    private String providerCode;
    private String subject;
    private Instant lastLoginAt;
}
