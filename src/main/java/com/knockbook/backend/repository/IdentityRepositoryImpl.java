package com.knockbook.backend.repository;

import com.knockbook.backend.domain.Identity;
import com.knockbook.backend.entity.IdentityEntity;
import com.knockbook.backend.entity.IdentityEntity_;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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

    @Override
    public Optional<Identity> findByProviderCodeAndSubject(String providerCode, String subject) {
        var cb = em.getCriteriaBuilder();
        var query = cb.createQuery(IdentityEntity.class);
        var root = query.from(IdentityEntity.class);

        query.select(root)
                .where(cb.and(
                        cb.equal(root.get(IdentityEntity_.providerCode), providerCode),
                        cb.equal(root.get(IdentityEntity_.subject), subject)
                ));

        return em.createQuery(query)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .map(entity -> Identity.builder()
                        .id(entity.getId())
                        .userId(entity.getUserId())
                        .providerCode(entity.getProviderCode())
                        .subject(entity.getSubject())
                        .lastLoginAt(entity.getLastLoginAt())
                        .build());
    }
}


