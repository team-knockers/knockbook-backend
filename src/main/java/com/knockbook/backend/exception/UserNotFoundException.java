package com.knockbook.backend.exception;

public class UserNotFoundException extends ApplicationException {

    public UserNotFoundException(Long userId) {
        super("USER_NOT_FOUND", "User not found: id=%d".formatted(userId));
    }
}
