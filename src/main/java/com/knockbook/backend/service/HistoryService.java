package com.knockbook.backend.service;

import com.knockbook.backend.domain.BookPreferCategoryStat;
import com.knockbook.backend.domain.BookPurchaseHistoryDetails;
import com.knockbook.backend.domain.BookReadCountStat;
import com.knockbook.backend.domain.BookRentalHistoryDetails;
import com.knockbook.backend.exception.BookNotFoundException;
import com.knockbook.backend.exception.CategoryNotFoundException;
import com.knockbook.backend.repository.BookCategoryRepository;
import com.knockbook.backend.repository.BookPurchaseHistoryRepository;
import com.knockbook.backend.repository.BookRentalHistoryRepository;
import com.knockbook.backend.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final BookPurchaseHistoryRepository purchaseRepo;
    private final BookRentalHistoryRepository rentalRepo;
    private final BookRepository bookRepository;
    private final BookCategoryRepository bookCategoryRepository;

    private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");

    @Transactional(readOnly = true)
    public List<BookPurchaseHistoryDetails> listPurchasesByUser(Long userId) {
        final var histories = purchaseRepo.findAllByUserId(userId);
        return histories.stream().map(h -> {
            final var bookId = h.getBookId();
            final var book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new BookNotFoundException(bookId.toString()));

            return BookPurchaseHistoryDetails.builder()
                    .history(h)
                    .bookTitle(book.getTitle())
                    .bookAuthor(book.getAuthor())
                    .bookImageUrl(book.getCoverImageUrl())
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<BookRentalHistoryDetails> listRentalsByUser(Long userId) {
        final var histories = rentalRepo.findAllByUserId(userId);
        return histories.stream().map(h -> {
            final var bookId = h.getBookId();
            final var book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new BookNotFoundException(bookId.toString()));

            return BookRentalHistoryDetails.builder()
                    .history(h)
                    .bookTitle(book.getTitle())
                    .bookAuthor(book.getAuthor())
                    .bookImageUrl(book.getCoverImageUrl())
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public BookReadCountStat getMyReadCountAndMembersAvgMonthly(long userId, int year, int month) {
        final var fromLdt = LocalDate.of(year, month, 1).atStartOfDay();
        final var toLdt   = fromLdt.plusMonths(1);
        final var from = fromLdt.atZone(ZONE_SEOUL).toInstant();
        final var to   = toLdt.atZone(ZONE_SEOUL).toInstant();

        final var purchase = purchaseRepo.aggregateCountsByUserBetween(from, to); // List<UserCount>
        final var rental   = rentalRepo.aggregateCountsByUserBetween(from, to);

        final var map = new HashMap<Long, Long>(Math.max(purchase.size(), rental.size()) * 2);
        purchase.forEach(uc -> map.merge(uc.userId(), uc.count(), Long::sum));
        rental.forEach(uc -> map.merge(uc.userId(), uc.count(), Long::sum));

        final var activeUsers = map.size();
        final var totalBooks  = map.values().stream().mapToLong(Long::longValue).sum();
        final var avg = (activeUsers == 0) ? 0.0 : (double) totalBooks / activeUsers;
        final var myCount = Math.toIntExact(map.getOrDefault(userId, 0L));

        return BookReadCountStat.builder()
                .yearAt(year)
                .monthAt(month)
                .readCountByMe(myCount)
                .avgReadCountByMember(avg)
                .build();
    }

    @Transactional(readOnly = true)
    public BookPreferCategoryStat getMyCategoryPreferenceAll(final Long userId) {

        final var purchases = purchaseRepo.findAllByUserId(userId);
        final var rentals   = rentalRepo.findAllByUserId(userId);

        final var uniqueBookIds = new HashSet<Long>(Math.max(purchases.size(), rentals.size()) * 2);
        purchases.forEach(h -> uniqueBookIds.add(h.getBookId()));
        rentals.forEach(h -> uniqueBookIds.add(h.getBookId()));

        if (uniqueBookIds.isEmpty()) {
            return BookPreferCategoryStat.builder()
                    .bookCategoryDisplayNameAndReadRatePair(Map.of())
                    .build();
        }

        final var categoryCounts = new HashMap<String, Integer>();
        int totalUniqueBooks = 0;

        final var categoryNameCache = new HashMap<Long, String>();

        for (final var bookId : uniqueBookIds) {
            final var bookOpt = bookRepository.findById(bookId);
            if (bookOpt.isEmpty()) { continue; }

            final var book = bookOpt.get();
            final var categoryId = book.getCategoryId();

            String displayName = categoryNameCache.get(categoryId);
            if (displayName == null) {
                final var category = bookCategoryRepository.findBy(categoryId)
                        .orElseThrow(() -> new CategoryNotFoundException(categoryId.toString()));
                displayName = category.getDisplayName();
                categoryNameCache.put(categoryId, displayName);
            }

            categoryCounts.merge(displayName, 1, Integer::sum);
            ++totalUniqueBooks;
        }

        if (totalUniqueBooks == 0) {
            return BookPreferCategoryStat.builder()
                    .bookCategoryDisplayNameAndReadRatePair(Map.of())
                    .build();
        }

        final var result = new LinkedHashMap<String, Double>(categoryCounts.size() * 2);
        for (var e : categoryCounts.entrySet()) {
            final var name  = e.getKey();
            final var count = e.getValue();
            final double pct = (count * 100.0) / totalUniqueBooks;
            result.put(name, pct);
        }

        return BookPreferCategoryStat.builder()
                .bookCategoryDisplayNameAndReadRatePair(result)
                .build();
    }
}
