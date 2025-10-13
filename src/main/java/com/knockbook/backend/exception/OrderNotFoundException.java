package com.knockbook.backend.exception;

public class OrderNotFoundException extends ApplicationException {

    public OrderNotFoundException(Long orderId) {
        super("ORDER_NOT_FOUND", "Order not found: id=%d".formatted(orderId));
    }
}
