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
                cb.equal(root.get("bookId"), bookId)
        ));

        final var q = em.createQuery(cq);
        q.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        final var list = q.getResultList();

        if (list.isEmpty()) {
            final var e = BookPurchaseHistoryEntity.builder()
                    .userId(userId).bookId(bookId)
                    .bookTitle(title).bookAuthor(author).bookImageUrl(imageUrl)
                    .purchaseCount(1)
                    .firstPurchasedAt(Date.from(purchasedAt))
                    .lastPurchasedAt(Date.from(purchasedAt))
                    .createdAt(new Date()).updatedAt(new Date())
                    .build();
            em.persist(e);
        } else {
            var e = list.get(0);
            e.setBookTitle(title);
            e.setBookAuthor(author);
            e.setBookImageUrl(imageUrl);
            e.setPurchaseCount(e.getPurchaseCount() + 1);
            e.setLastPurchasedAt(Date.from(purchasedAt));
            e.setUpdatedAt(new Date());
            em.merge(e);
        }
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
