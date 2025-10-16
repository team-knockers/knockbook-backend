package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PointTransaction {

    public enum Kind { EARN, SPEND, EXPIRE, ADJUST }

    private Long id;
    private Long userId;
    private Kind kind;
    private Integer amountSigned;
    private Instant expiresAt;
    private Long orderId;
    private String memo;
    private Instant createdAt;
}
