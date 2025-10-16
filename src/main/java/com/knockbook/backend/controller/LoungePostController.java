package com.knockbook.backend.controller;

import com.knockbook.backend.dto.*;
import com.knockbook.backend.service.LoungePostService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping(path = "/lounge")
@Validated
public class LoungePostController {

    @Autowired
    private LoungePostService loungePostService;

    // API-LOUNGE-01
    @PreAuthorize("#userId == authentication.name")
    @GetMapping(path = "/{userId}")
    public ResponseEntity<GetLoungePostSummaryResponse> getLoungePostsSummary(
            @PathVariable("userId") String userId,
            @RequestParam("page") @Min(value = 1) int page,
            @RequestParam("size") @Min(value = 1) @Max(value = 50) int size,
            @RequestParam(required = false, defaultValue ="newest") String sortBy
    ) {

        // 1) Create PageRequest
        final var zeroBasedPage = page - 1;
        final var sort = Sort.by(sortBy);
        final var pageable = PageRequest.of(zeroBasedPage, size, sort);

        // 2) Retrieve paged LoungePostSummary from domain
        final var postPage = loungePostService.getPostsSummary(pageable);

        // 3) Map domain object to DTO
        final var dtoPage = postPage.map(p ->
                LoungePostSummaryDto.builder()
                        .id(String.valueOf(p.getId()))
                        .displayName(p.getDisplayName())
                        .title(p.getTitle())
                        .previewImageUrl(p.getPreviewImageUrl())
                        .likeCount(p.getLikeCount())
                        .createdAt(toLocalDate(p.getCreatedAt()))
                        .build());

        // 4) Build LoungePostSummaryResponse
        final var response = GetLoungePostSummaryResponse.builder()
                .posts(dtoPage.getContent())
                .page(dtoPage.getNumber()+1)
                .size(dtoPage.getSize())
                .totalItems((int) dtoPage.getTotalElements())
                .totalPages(dtoPage.getTotalPages())
                .build();

        // 5) Return final response
        return ResponseEntity.ok(response);
    }

    // API-LOUNGE-02
    @PreAuthorize("#userId == authentication.name")
    @GetMapping(path = "/{userId}/{postId}")
    public ResponseEntity<GetLoungePostDetailsResponse> getLoungePostDetails(
            @PathVariable("userId") String userId,
            @PathVariable("postId") String postId
    ) {
        // 1) Convert input ID (String -> Long)
        final var id = Long.valueOf(postId);

        // 2) Retrieve LoungePostDetails from domain
        final var postDetails = loungePostService.getPostDetails(id);

        // 3) Map domain object to DTO
        final var response = GetLoungePostDetailsResponse.builder()
                .id(String.valueOf(postDetails.getId()))
                .displayName(postDetails.getDisplayName())
                .avatarUrl(postDetails.getAvatarUrl())
                .bio(postDetails.getBio())
                .title(postDetails.getTitle())
                .subtitle(postDetails.getSubtitle())
                .content(postDetails.getContent())
                .likeCount(postDetails.getLikeCount())
                .createdAt(toLocalDate(postDetails.getCreatedAt()))
                .build();

        // 4) Return final response
        return ResponseEntity.ok(response);
    }

    // Helper: Change Instant to LocalDate
    private static LocalDate toLocalDate(Instant instant) {
        return instant == null ? null : LocalDate.ofInstant(instant, ZoneId.of("Asia/Seoul"));
    }
}
