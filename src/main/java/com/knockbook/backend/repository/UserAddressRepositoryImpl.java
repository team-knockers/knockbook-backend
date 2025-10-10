package com.knockbook.backend.repository;

import com.knockbook.backend.domain.UserAddress;
import com.knockbook.backend.entity.QUserAddressEntity;
import com.knockbook.backend.entity.UserAddressEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserAddressRepositoryImpl implements UserAddressRepository {

    private final EntityManager em;
    private final JPAQueryFactory qf;

    @Override
    @Transactional
    public UserAddress insert(UserAddress domain) {
        final var entity = UserAddressEntity.fromDomain(domain);
        em.persist(entity);
        em.flush();
        return entity.toDomain();
    }

    @Override
    @Transactional
    public void update(UserAddress patch) {
        final var id = patch.getId();
        if (id == null) {
            throw new IllegalArgumentException("id required");
        }

        final var ua = QUserAddressEntity.userAddressEntity;
        var upd = qf.update(ua).where(ua.id.eq(patch.getId()));

        var changed = false;
        if (patch.getLabel() != null) {
            upd.set(ua.label, patch.getLabel());
            changed = true;
        }

        if (patch.getRecipientName() != null) {
            upd.set(ua.recipientName, patch.getRecipientName());
            changed = true;
        }

        if (patch.getPhone() != null) {
            upd.set(ua.phone, patch.getPhone());
            changed = true;
        }

        if (patch.getPostalCode() != null) {
            upd.set(ua.postalCode, patch.getPostalCode());
            changed = true;
        }

        if (patch.getAddress1() != null) {
            upd.set(ua.address1, patch.getAddress1());
            changed = true;
        }

        if (patch.getAddress2() != null) {
            upd.set(ua.address2, patch.getAddress2());
            changed = true;
        }

        if (patch.getEntryInfo() != null) {
            upd.set(ua.entryInfo, patch.getEntryInfo());
            changed = true;
        }

        if (patch.getDeliveryMemo() != null) {
            upd.set(ua.deliveryMemo, patch.getDeliveryMemo());
            changed = true;
        }

        if (!changed) { return; }
        upd.execute();
    }

    @Override
    public Optional<UserAddress> findById(Long id) {
        final var entity = em.find(UserAddressEntity.class, id);
        return Optional.ofNullable(entity.toDomain());
    }

    @Override
    public List<UserAddress> findByUserId(Long userId) {
        final var ua = QUserAddressEntity.userAddressEntity;
        return qf.selectFrom(ua)
                .where(ua.userId.eq(userId))
                .orderBy(ua.isDefault.desc(), ua.createdAt.desc())
                .fetch()
                .stream()
                .map(UserAddressEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<UserAddress> findDefaultByUserId(Long userId) {
        final var ua = QUserAddressEntity.userAddressEntity;
        final var entity = qf
                .selectFrom(ua)
                .where(ua.userId.eq(userId), ua.isDefault.isTrue())
                .fetchOne();

        if (entity == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(entity.toDomain());
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long addressId) {
        final var ua = QUserAddressEntity.userAddressEntity;
        qf.update(ua).set(ua.isDefault, false)
                .where(ua.userId.eq(userId), ua.isDefault.isTrue())
                .execute();
        qf.update(ua).set(ua.isDefault, true)
                .where(ua.id.eq(addressId), ua.userId.eq(userId))
                .execute();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        final var found = em.find(UserAddressEntity.class, id);
        if (found != null) {
            em.remove(found);
        }
    }
}
