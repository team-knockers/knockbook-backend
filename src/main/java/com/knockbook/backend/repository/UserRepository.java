package com.knockbook.backend.repository;

import com.knockbook.backend.domain.User;

import java.util.Optional;

public interface UserRepository {

    User insert(final String email,
                final String displayName);

    Optional<User> findById(final Long id);

    void update(final User patch);
}
