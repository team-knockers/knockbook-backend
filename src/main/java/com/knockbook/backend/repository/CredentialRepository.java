package com.knockbook.backend.repository;

import com.knockbook.backend.domain.Credential;

import java.util.Optional;

public interface CredentialRepository {

    Credential insert(final Long identityId,
                      final String passwordHash);

    Optional<Credential> findByIdentityId(final Long identityId);

    void update(final Long identityId,
                final String passwordHash);
}
