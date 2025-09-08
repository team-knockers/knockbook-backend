package com.knockbook.backend.repository;

import com.knockbook.backend.domain.Identity;

public interface IdentityRepository {
    Identity insert(final Long userId,
                    final String providerCode,
                    final String subject);
}
