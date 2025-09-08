package com.knockbook.backend.repository;

import com.knockbook.backend.domain.User;
import com.knockbook.backend.entity.UserEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final EntityManager em;

    @Override
    public User insert(String email, String displayName) {
        final var userEntity = UserEntity.builder()
                .email(email)
                .displayName(displayName)
                .status(UserEntity.Status.ACTIVE)
                .build();
        em.persist(userEntity);  // insert (Id generates automatically)
        em.flush(); // get PK
        return User.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .displayName(userEntity.getDisplayName())
                .build();
    }
}
