package com.knockbook.backend.exception;

public class CredentialNotFoundException extends ApplicationException {

    public CredentialNotFoundException(Long identityId) {
        super("CREDENTIAL_NOT_FOUND", "Credential not found: identityId=%d".formatted(identityId));
    }
}
