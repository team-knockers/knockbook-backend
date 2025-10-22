package com.knockbook.backend.repository;

import com.knockbook.backend.domain.BookRentalHistory;
import com.knockbook.backend.domain.UserBookOrderCount;
import com.knockbook.backend.entity.BookEntity;
import com.knockbook.backend.entity.BookRentalHistoryEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class BookRentalHistoryRepositoryImpl implements BookRentalHistoryRepository{

    private final EntityManager em;

    @Override
    @Transactional
    public void upsertRental(final Long userId,
                             final Long orderId,
                             final Long bookId,
                             final Instant rentalStart,
                             final Instant rentalEnd,
                             final int rentalDays) {

        final var cb = em.getCriteriaBuilder();
        final var cq = cb.createQuery(BookRentalHistoryEntity.class);
        final var root = cq.from(BookRentalHistoryEntity.class);
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
        final var e = BookRentalHistoryEntity.builder()
                .userId(userId).orderId(orderId).bookId(bookId)
                .rentalCount(1)
                .lastRentalStartAt(Date.from(rentalStart))
                .lastRentalEndAt(Date.from(rentalEnd))
                .lastRentalDays(rentalDays)
                .createdAt(now).updatedAt(now)
                .build();
        em.persist(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookRentalHistory> findAllByUserId(Long userId) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(BookRentalHistoryEntity.class);
        var root = cq.from(BookRentalHistoryEntity.class);
        cq.select(root).where(cb.equal(root.get("userId"), userId))
                .orderBy(cb.desc(root.get("lastRentalStartAt")));
        var entities = em.createQuery(cq).getResultList();

        if (entities.isEmpty()) {
            return List.of();
        }

        return entities.stream().map(BookRentalHistoryEntity::toDomain).toList();
    }

    @Override
    public List<UserBookOrderCount> aggregateCountsByUserBetween(final Instant fromInclusive,
                                                                 final Instant toExclusive) {
        final var cb = em.getCriteriaBuilder();
        final var cq = cb.createTupleQuery();
        final var root = cq.from(BookRentalHistoryEntity.class);

        final var fromDate = Date.from(fromInclusive);
        final var toDate   = Date.from(toExclusive);

        cq.multiselect(
                        root.get("userId").alias("userId"),
                        cb.count(root).alias("cnt")
                )
                .where(cb.and(
                        cb.greaterThanOrEqualTo(root.get("lastRentalStartAt"), fromDate),
                        cb.lessThan(root.get("lastRentalStartAt"), toDate)
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

