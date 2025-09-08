package com.knockbook.backend.repository;

import com.knockbook.backend.domain.Credential;
import com.knockbook.backend.entity.CredentialEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CredentialRepositoryImpl implements CredentialRepository {

    private final EntityManager em; // no need to put AutoWired annotation if it's final

    @Override
    public Credential insert(Long identityId, String passwordHash) {
        final var credentialEntity = CredentialEntity.builder()
                .identityId(identityId)
                .passwordHash(passwordHash)
                .build();

        em.persist(credentialEntity);
        em.flush(); // get PK
        em.refresh(credentialEntity);

        return Credential.builder()
                .identityId(credentialEntity.getIdentityId())
                .passwordHash(credentialEntity.getPasswordHash())
                .passwordUpdatedAt(credentialEntity.getPasswordUpdatedAt())
                .build();
    }
}
