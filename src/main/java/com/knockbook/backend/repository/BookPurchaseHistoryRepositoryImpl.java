package com.knockbook.backend.repository;

import com.knockbook.backend.domain.BookPurchaseHistory;
import com.knockbook.backend.entity.BookPurchaseHistoryEntity;
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
public class BookPurchaseHistoryRepositoryImpl implements BookPurchaseHistoryRepository{

    private final EntityManager em;

    @Override
    @Transactional
    public void upsertPurchase(final Long userId,
                               final Long orderId,
                               final Long bookId,
                               final String title,
                               final String author,
                               final String imageUrl,
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
                .bookTitle(title).bookAuthor(author).bookImageUrl(imageUrl)
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
        cq.select(root)
                .where(cb.equal(root.get("userId"), userId))
                .orderBy(cb.desc(root.get("lastPurchasedAt")));
        return em.createQuery(cq).getResultList()
                .stream().map(BookPurchaseHistoryEntity::toDomain).toList();
    }
}

