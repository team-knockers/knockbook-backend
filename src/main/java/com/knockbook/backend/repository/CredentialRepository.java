package com.knockbook.backend.repository;

import com.knockbook.backend.domain.Credential;

public interface CredentialRepository {
    Credential insert(final Long identityId,
                      final String passwordHash);
}
