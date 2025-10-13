package com.knockbook.backend.exception;

public class InvalidCartItemsException extends ApplicationException {

    public InvalidCartItemsException() {
        super("INVALID_CART_ITEMS", "Invalid cart items");
    }
}
