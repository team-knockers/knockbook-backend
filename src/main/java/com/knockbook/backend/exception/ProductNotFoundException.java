package com.knockbook.backend.exception;

public class ProductNotFoundException extends ApplicationException {

    public ProductNotFoundException(Long productId) {
        super("PRODUCT_NOT_FOUND", "Product not found: id=%d".formatted(productId));
    }
}
