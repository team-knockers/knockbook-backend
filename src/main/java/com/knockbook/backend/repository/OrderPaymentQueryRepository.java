package com.knockbook.backend.repository;

import com.knockbook.backend.domain.OrderPayment;

import java.util.Optional;

public interface OrderPaymentQueryRepository {
    Optional<OrderPayment> findReadyByOrderId(final Long orderId);
    Optional<OrderPayment> findByTxId(final String txId);
}
