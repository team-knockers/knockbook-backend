package com.knockbook.backend.repository;

import com.knockbook.backend.domain.BookPurchaseHistory;

import java.time.Instant;
import java.util.List;

public interface BookPurchaseHistoryRepository {
    void upsertPurchase(final Long userId,
                        final Long bookId,
                        final String title,
                        final String author,
                        final String imageUrl,
                        final Instant purchasedAt);
    List<BookPurchaseHistory> findAllByUserId(Long userId);
}
