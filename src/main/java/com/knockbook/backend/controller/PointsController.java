package com.knockbook.backend.controller;

import com.knockbook.backend.dto.PointTransactionResponse;
import com.knockbook.backend.dto.PointsBalanceResponse;
import com.knockbook.backend.service.PointsService;
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
@RequestMapping("/users/{userId}/points")
public class PointsController {

    private final PointsService pointsService;

    @GetMapping("/balance")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<PointsBalanceResponse> getBalance(
            @PathVariable final String userId) {

        final var balance = pointsService.getAvailablePoints(Long.valueOf(userId));
        final var dto = PointsBalanceResponse.builder()
                .balance(balance)
                .build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/transactions")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<List<PointTransactionResponse>> getTransactions(
            @PathVariable final String userId) {

        final var userIdLong = Long.valueOf(userId);
        final var transactions = pointsService.getUserTransactions(userIdLong);
        final var dto = transactions.stream()
                .map(PointTransactionResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(dto);
    }
}
