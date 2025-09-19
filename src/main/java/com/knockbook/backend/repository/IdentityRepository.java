package com.knockbook.backend.repository;

import com.knockbook.backend.domain.Identity;

import java.util.Optional;

public interface IdentityRepository {

    Identity insert(final Long userId,
                    final String providerCode,
                    final String subject);

    Optional<Identity> findByUserId(final Long userId);

    Optional<Identity> findByProviderCodeAndSubject(final String providerCode,
                                                    final String subject);
}
