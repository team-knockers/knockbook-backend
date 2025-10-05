package com.knockbook.backend.exception;

public class OpenCartNotFoundException extends ApplicationException {

    public OpenCartNotFoundException(Long userId) {
        super("OPEN_CART_NOT_FOUND", "Open cart not found: id=%d".formatted(userId));
    }
}
