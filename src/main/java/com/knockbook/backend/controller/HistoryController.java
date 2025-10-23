package com.knockbook.backend.controller;

import com.knockbook.backend.dto.*;
import com.knockbook.backend.service.BookService;
import com.knockbook.backend.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/history/books")
public class HistoryController {

    private final HistoryService historyService;
    private final BookService bookService;

    @GetMapping("/purchases")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<List<BookPurchaseHistoryDTO>> listBookPurchases(
            @PathVariable final String userId) {
        final var list = historyService.listPurchasesByUser(Long.valueOf(userId))
                .stream()
                .map(BookPurchaseHistoryDTO::fromDomain).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/rentals")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<List<BookRentalHistoryDTO>> listBookRentals(
            @PathVariable final String userId) {
        final var list = historyService.listRentalsByUser(Long.valueOf(userId))
                .stream().map(BookRentalHistoryDTO::fromDomain).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/stat/average")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<List<BookReadCountStatDTO>> getAverageInPeriod(
            @PathVariable final String userId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        final var userIdLong = Long.valueOf(userId);
        final var startYm = YearMonth.from(from);
        final var endYm   = YearMonth.from(to);

        final var results = new ArrayList<BookReadCountStatDTO>();
        for (var ym = startYm; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
            final var stat = historyService.getMyReadCountAndMembersAvgMonthly(
                    userIdLong, ym.getYear(), ym.getMonthValue());
            results.add(BookReadCountStatDTO.fromDomain(stat));
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/stat/category-preference")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<List<BookPreferCategoryStatDTO>> getCategoryPreferenceAll(
            @PathVariable final String userId) {

        final var stat = historyService.getMyCategoryPreferenceAll(Long.valueOf(userId));
        final var list = stat.getBookCategoryDisplayNameAndReadRatePair().entrySet().stream()
                .map(e -> BookPreferCategoryStatDTO.of(e.getKey(), e.getValue()))
                .toList();
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/reviews")
    public ResponseEntity<List<BookReviewSummaryDTO>> getAllReviews(
            @PathVariable("userId") String userId
    ) {
        final var userIdLong = Long.valueOf(userId);
        final var reviews = bookService.getAllReviewsByUser(userIdLong);
        final var dtos = reviews.stream().map(BookReviewSummaryDTO::from).toList();
        return ResponseEntity.ok(dtos);
    }
}
