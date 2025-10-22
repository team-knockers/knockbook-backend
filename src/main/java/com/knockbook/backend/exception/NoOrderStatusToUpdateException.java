package com.knockbook.backend.exception;

public class NoOrderStatusToUpdateException extends ApplicationException {

    public NoOrderStatusToUpdateException(Long orderId) {
        super("NO_ORDER_STATUS_TO_UPDATE", "No order status to update: order id=%d".formatted(orderId));
    }
}
