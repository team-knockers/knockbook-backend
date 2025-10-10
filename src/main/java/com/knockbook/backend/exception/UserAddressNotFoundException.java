package com.knockbook.backend.exception;

public class UserAddressNotFoundException extends ApplicationException {

    public UserAddressNotFoundException(Long addressId) {
        super("USER_ADDRESS_NOT_FOUND", "User Address not found: id=%d".formatted(addressId));
    }
}
