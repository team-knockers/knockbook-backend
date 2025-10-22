package com.knockbook.backend.repository;

import com.knockbook.backend.domain.Book;
import com.knockbook.backend.domain.BookPurchaseHistory;
import com.knockbook.backend.domain.UserBookOrderCount;
import com.knockbook.backend.entity.BookEntity;
import com.knockbook.backend.entity.BookPurchaseHistoryEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class BookPurchaseHistoryRepositoryImpl implements BookPurchaseHistoryRepository{

    private final EntityManager em;

    @Override
    @Transactional
    public void upsertPurchase(final Long userId,
                               final Long orderId,
                               final Long bookId,
                               final Instant purchasedAt) {

        final var cb = em.getCriteriaBuilder();
        final var cq = cb.createQuery(BookPurchaseHistoryEntity.class);
        final var root = cq.from(BookPurchaseHistoryEntity.class);
        cq.select(root).where(cb.and(
                cb.equal(root.get("userId"), userId),
                cb.equal(root.get("orderId"), orderId),
                cb.equal(root.get("bookId"), bookId)
        ));
        final var exists = !em.createQuery(cq)
                .setLockMode(LockModeType.PESSIMISTIC_READ)
                .setMaxResults(1).getResultList().isEmpty();
        if (exists) { return; }

        final var now = new Date();
        final var e = BookPurchaseHistoryEntity.builder()
                .userId(userId).orderId(orderId).bookId(bookId)
                .purchaseCount(1)
                .firstPurchasedAt(Date.from(purchasedAt))
                .lastPurchasedAt(Date.from(purchasedAt))
                .createdAt(now).updatedAt(now)
                .build();
        em.persist(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookPurchaseHistory> findAllByUserId(final Long userId) {
        final var cb = em.getCriteriaBuilder();
        final var cq = cb.createQuery(BookPurchaseHistoryEntity.class);
        final var root = cq.from(BookPurchaseHistoryEntity.class);
        cq.select(root).where(cb.equal(root.get("userId"), userId))
                .orderBy(cb.desc(root.get("lastPurchasedAt")));
        final var entities = em.createQuery(cq).getResultList();

        if (entities.isEmpty()) { return List.of(); }

        return entities.stream().map(BookPurchaseHistoryEntity::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBookOrderCount> aggregateCountsByUserBetween(final Instant fromInclusive,
                                                                 final Instant toExclusive) {
        final var cb = em.getCriteriaBuilder();
        final var cq = cb.createTupleQuery();
        final var root = cq.from(BookPurchaseHistoryEntity.class);

        final var fromDate = Date.from(fromInclusive);
        final var toDate   = Date.from(toExclusive);

        cq.multiselect(
                        root.get("userId").alias("userId"),
                        cb.count(root).alias("cnt")
                )
                .where(cb.and(
                        cb.greaterThanOrEqualTo(root.get("lastPurchasedAt"), fromDate),
                        cb.lessThan(root.get("lastPurchasedAt"), toDate)
                ))
                .groupBy(root.get("userId"));

        return em.createQuery(cq).getResultList().stream()
                .map(t -> new UserBookOrderCount(
                        t.get("userId", Long.class),
                        (Long) t.get("cnt")
                ))
                .toList();
    }
}

