package com.knockbook.backend.repository;

import com.knockbook.backend.domain.OrderPayment;

public interface OrderPaymentRepository {
    OrderPayment save(OrderPayment record);
}
