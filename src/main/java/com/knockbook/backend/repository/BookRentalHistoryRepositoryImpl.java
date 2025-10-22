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
                cb.equal(root.get("bookId"), bookId)
        ));

        final var q = em.createQuery(cq);
        q.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        final var list = q.getResultList();

        if (list.isEmpty()) {
            final var e = BookRentalHistoryEntity.builder()
                    .userId(userId).bookId(bookId)
                    .bookTitle(title).bookAuthor(author).bookImageUrl(imageUrl)
                    .rentalCount(1)
                    .lastRentalStartAt(Date.from(rentalStart))
                    .lastRentalEndAt(Date.from(rentalEnd))
                    .lastRentalDays(rentalDays)
                    .createdAt(new Date()).updatedAt(new Date())
                    .build();
            em.persist(e);
        } else {
            final var e = list.get(0);
            e.setBookTitle(title);
            e.setBookAuthor(author);
            e.setBookImageUrl(imageUrl);
            e.setRentalCount(e.getRentalCount() + 1);
            e.setLastRentalStartAt(Date.from(rentalStart));
            e.setLastRentalEndAt(Date.from(rentalEnd));
            e.setLastRentalDays(rentalDays);
            e.setUpdatedAt(new Date());
            em.merge(e);
        }
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
