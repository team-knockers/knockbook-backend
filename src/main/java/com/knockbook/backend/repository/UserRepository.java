package com.knockbook.backend.repository;

import com.knockbook.backend.domain.User;

public interface UserRepository {
    User insert(final String email,
                final String displayName);
}
