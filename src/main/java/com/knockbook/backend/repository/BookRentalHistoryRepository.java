package com.knockbook.backend.repository;

import com.knockbook.backend.domain.BookRentalHistory;
import com.knockbook.backend.domain.UserBookOrderCount;

import java.time.Instant;
import java.util.List;

public interface BookRentalHistoryRepository {
    void upsertRental(final Long userId,
                      final Long orderId,
                      final Long bookId,
                      final Instant rentalStart,
                      final Instant rentalEnd, int rentalDays);

    List<BookRentalHistory> findAllByUserId(Long userId);

    List<UserBookOrderCount> aggregateCountsByUserBetween(final Instant fromInclusive,
                                                          final Instant toExclusive);
}
