package com.knockbook.backend.repository;

import com.knockbook.backend.domain.Identity;
import com.knockbook.backend.entity.IdentityEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IdentityRepositoryImpl implements IdentityRepository {

    private final EntityManager em;

    @Override
    public Identity insert(Long userId, String providerCode, String subject) {
        final var identityEntity = IdentityEntity.builder()
                .userId(userId)
                .providerCode(providerCode)
                .subject(subject)
                .build();

        em.persist(identityEntity); // insert
        em.flush(); // get PK

        return Identity.builder()
                .id(identityEntity.getId())
                .userId(identityEntity.getUserId())
                .providerCode(identityEntity.getProviderCode())
                .subject(identityEntity.getSubject())
                .build();
    }
}
