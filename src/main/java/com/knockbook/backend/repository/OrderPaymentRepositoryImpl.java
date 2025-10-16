package com.knockbook.backend.repository;

import com.knockbook.backend.domain.OrderPayment;
import com.knockbook.backend.entity.OrderPaymentEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderPaymentRepositoryImpl implements OrderPaymentRepository {

    private final EntityManager em;

    @Override @Transactional
    public OrderPayment save(OrderPayment domain) {
        var entity = OrderPaymentEntity.toEntity(domain);
        if (entity.getId() == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }
        return entity.toDomain();
    }
}

