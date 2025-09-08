package com.knockbook.backend.repository;

import com.knockbook.backend.domain.Credential;
import com.knockbook.backend.entity.CredentialEntity;
import com.knockbook.backend.entity.CredentialEntity_;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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

    @Override
    public Optional<Credential> findByIdentityId(Long identityId) {
        final var cb = em.getCriteriaBuilder();
        final var query = cb.createQuery(CredentialEntity.class);
        final var root = query.from(CredentialEntity.class);

        query.select(root)
                .where(cb.equal(root.get(CredentialEntity_.identityId), identityId));

        return em.createQuery(query)
                .getResultStream()
                .findFirst()
                .map(entity -> Credential.builder()
                        .identityId(entity.getIdentityId())
                        .passwordHash(entity.getPasswordHash())
                        .passwordUpdatedAt(entity.getPasswordUpdatedAt())
                        .build());
    }
}
