package com.knockbook.backend.exception;

public class AddressNotBelongToUserException extends ApplicationException {

    public AddressNotBelongToUserException(Long userId) {
        super("USER_NOT_BELONG_TO_USER", "address does not belong to user: id=%d".formatted(userId));
    }
}
