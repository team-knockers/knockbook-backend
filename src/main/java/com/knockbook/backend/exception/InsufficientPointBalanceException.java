package com.knockbook.backend.exception;

public class InsufficientPointBalanceException extends ApplicationException {

    public InsufficientPointBalanceException(final Long userId) {
        super(
                "INSUFFICIENT_POINT_BALANCE",
                "Insufficient point balance: userId=%d".formatted(userId));
    }
}
