package com.knockbook.backend.controller;

import com.knockbook.backend.dto.BookPurchaseHistoryDTO;
import com.knockbook.backend.dto.BookRentalHistoryDTO;
import com.knockbook.backend.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/history/books")
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping("/purchases")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<List<BookPurchaseHistoryDTO>> listBookPurchases(
            @PathVariable final String userId) {
        final var list = historyService.listPurchasesByUser(Long.valueOf(userId))
                .stream().map(BookPurchaseHistoryDTO::fromDomain).toList();
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
}
