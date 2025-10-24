package com.knockbook.backend.controller;

import com.knockbook.backend.dto.ReviewedItemDTO;
import com.knockbook.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/reviews")
@RequiredArgsConstructor
public class UserReviewController {

    private final ReviewService reviewService;

    @PreAuthorize("#userId == authentication.name")
    @GetMapping
    public ResponseEntity<List<ReviewedItemDTO>> getReviewedItemIds(
            @PathVariable("userId") String userId) {
        final var userIdLong = Long.valueOf(userId);
        final var result = reviewService.getReviewedItemIdsAll(userIdLong);
        final var dtos = result.stream().map(ReviewedItemDTO::fromModel).toList();
        return ResponseEntity.ok(dtos);
    }
}
