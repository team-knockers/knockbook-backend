package com.knockbook.backend.repository;

import com.knockbook.backend.domain.BookPurchaseHistory;
import com.knockbook.backend.domain.UserBookOrderCount;

import java.time.Instant;
import java.util.List;

public interface BookPurchaseHistoryRepository {
    void upsertPurchase(final Long userId,
                        final Long orderId,
                        final Long bookId,
                        final Instant purchasedAt);

    List<BookPurchaseHistory> findAllByUserId(Long userId);

    List<UserBookOrderCount> aggregateCountsByUserBetween(final Instant fromInclusive,
                                                          final Instant toExclusive);
}
