package com.knockbook.backend.exception;

public class IdentityNotFoundException extends ApplicationException {

    public IdentityNotFoundException(Long identityId) {
        super("IDENTITY_NOT_FOUND", "Identity not found: id=%d".formatted(identityId));
    }

    public IdentityNotFoundException(String subject) {
        super("IDENTITY_NOT_FOUND", "Identity not found: subject=%s".formatted(subject));
    }
}
