package com.knockbook.backend.repository;

import com.knockbook.backend.domain.BookRentalHistory;
import com.knockbook.backend.entity.BookRentalHistoryEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookRentalHistoryRepositoryImpl implements BookRentalHistoryRepository{

    private final EntityManager em;

    @Override
    @Transactional
    public void upsertRental(final Long userId,
                             final Long orderId,
                             final Long bookId,
                             final String title,
                             final String author,
                             final String imageUrl,
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
                .bookTitle(title).bookAuthor(author).bookImageUrl(imageUrl)
                .rentalCount(1) // 항상 1로 고정
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
        final var cb = em.getCriteriaBuilder();
        final var cq = cb.createQuery(BookRentalHistoryEntity.class);
        final var root = cq.from(BookRentalHistoryEntity.class);
        cq.select(root)
                .where(cb.equal(root.get("userId"), userId))
                .orderBy(cb.desc(root.get("lastRentalStartAt")));
        return em.createQuery(cq).getResultList()
                .stream().map(BookRentalHistoryEntity::toDomain).toList();
    }
}

