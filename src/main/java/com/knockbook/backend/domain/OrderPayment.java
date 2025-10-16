package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OrderPayment {

    public enum Method { KAKAOPAY, TOSSPAY }
    public enum TxStatus { READY, APPROVED, PARTIAL_CANCELLED, CANCELLED, FAILED }

    private Long id;
    private Long orderId;
    private Method method;
    private String provider;
    private String txId;
    private Integer amount;
    private TxStatus status;
    private Instant approvedAt;
    private Instant cancelledAt;
    private Instant createdAt;
    private Instant updatedAt;
}
