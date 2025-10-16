package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PaymentApprovalResult {
    private final Long userId;
    private final Long orderId;
    private final OrderAggregate order;
    private final OrderPayment payment;
}
