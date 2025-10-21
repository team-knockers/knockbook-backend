package com.knockbook.backend.repository;

import com.knockbook.backend.domain.PointTransaction;
import com.knockbook.backend.entity.OrderEntity;
import com.knockbook.backend.entity.PointTransactionEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointTransactionRepositoryImpl implements PointTransactionRepository {

    private final EntityManager em;

    @Override
    @Transactional
    public PointTransaction save(PointTransaction tx) {
        var entity = PointTransactionEntity.toEntity(tx);
        if (entity.getId() == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }

        String orderNo = null;
        if (entity.getOrderId() != null) {
            final var order = em.find(OrderEntity.class, entity.getOrderId());
            if (order != null) {
                orderNo = order.getOrderNo();
            }
        }

        return entity.toDomain(orderNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointTransaction> findAllByUserId(Long userId) {
        final var cb = em.getCriteriaBuilder();
        final var cq = cb.createTupleQuery();
        final var root = cq.from(PointTransactionEntity.class);

        final var sq = cq.subquery(String.class);
        final var o = sq.from(OrderEntity.class);
        sq.select(o.get("orderNo"))
                .where(cb.equal(o.get("id"), root.get("orderId")));

        cq.multiselect(root, sq)
                .where(cb.equal(root.get("userId"), userId))
                .orderBy(cb.desc(root.get("createdAt")));

        return em.createQuery(cq).getResultList().stream().map(t -> {
            final var entity = t.get(0, PointTransactionEntity.class);
            final var orderNo = t.get(1, String.class);
            return entity.toDomain(orderNo);
        }).toList();
    }
}
