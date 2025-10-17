package com.knockbook.backend.controller;

import com.knockbook.backend.domain.LoungePostComment;
import com.knockbook.backend.dto.*;
import com.knockbook.backend.service.LoungePostService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/lounge")
@Validated
public class LoungePostController {

    @Autowired
    private LoungePostService loungePostService;

    // API-LOUNGE-01: Get a summary of lounge posts
    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{userId}")
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

    // API-LOUNGE-02: Get details of a single lounge post
    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{userId}/{postId}")
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

    // API-LOUNGE-03: Create a comment and return the updated page of comments
    @PreAuthorize("#userId == authentication.name")
    @PostMapping("/{userId}/{postId}/comments")
    public ResponseEntity<List<GetLoungePostCommentResponse>> createComment(
            @PathVariable("userId") String userId,
            @PathVariable("postId") String postId,
            @RequestBody String content,
            @PageableDefault(page = 0, size = 20, sort = "createdAt") Pageable pageable
    ) {
        // 1) Convert IDs to Long
        final var longPostId = Long.valueOf(postId);
        final var longUserId = Long.valueOf(userId);

        // 2) Create comment via service
        final var comment = loungePostService.createComment(longPostId, longUserId, content);

        // 3) Retrieve updated page of comments
        final List<LoungePostComment> comments = loungePostService.getCommentsByPostId(longPostId, pageable);

        // 4) Return response
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toDTOList(comments));
    }

    // API-LOUNGE-04: Get multiple comments with pagination (1-based page)
    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{userId}/{postId}/comments")
    public ResponseEntity<List<GetLoungePostCommentResponse>> getCommentsByPost(
            @PathVariable("userId") String userId,
            @PathVariable("postId") String postId,
            @RequestParam(defaultValue = "1") @Min(1) int page,  // 1-based 입력
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        // 1) Convert 1-based page to 0-based
        final var zeroBasedPage = page - 1;
        final var pageable = PageRequest.of(zeroBasedPage, size, Sort.by("createdAt").descending());

        // 2) Convert postId to Long
        final var longPostId = Long.valueOf(postId);

        // 3) Retrieve comments from service
        final var comments = loungePostService.getCommentsByPostId(longPostId, pageable);

        return ResponseEntity.ok(toDTOList(comments));
    }

    // API-LOUNGE-05: Get a single comment (for edit preview)
    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{userId}/comments/{commentId}")
    public ResponseEntity<GetLoungePostCommentResponse> getCommentById(
            @PathVariable("userId") String userId,
            @PathVariable("commentId") String commentId
    ) {
        final var longCommentId = Long.valueOf(commentId);
        final var comment = loungePostService.getComment(longCommentId);

        return ResponseEntity.ok(toDTO(comment));
    }

    // API-LOUNGE-06: Update a comment and return the updated page
    @PreAuthorize("#userId == authentication.name")
    @PutMapping("/{userId}/comments/{commentId}")
    public ResponseEntity<List<GetLoungePostCommentResponse>> updateComment(
            @PathVariable("userId") String userId,
            @PathVariable("commentId") String commentId,
            @RequestBody String content,
            @PageableDefault(page = 0, size = 20, sort = "createdAt") Pageable pageable
    ) {
        // 1) Convert postId to Long
        final var longCommentId = Long.valueOf(commentId);
        final var longUserId = Long.valueOf(userId);

        // 2) Update comment via service
        final var updated = loungePostService.updateComment(longCommentId, longUserId, content);

        // 3) Retrieve updated page of comment
        final var comments = loungePostService.getCommentsByPostId(updated.getPostId(), pageable);

        return ResponseEntity.ok(toDTOList(comments));
    }

    // API-LOUNGE-07: Delete a comment and return the updated page
    @PreAuthorize("#userId == authentication.name")
    @DeleteMapping("/{userId}/comments/{commentId}")
    public ResponseEntity<List<GetLoungePostCommentResponse>> deleteComment(
            @PathVariable("userId") String userId,
            @PathVariable("commentId") String commentId,
            @PageableDefault(page = 0, size = 20, sort = "createdAt") Pageable pageable
    ) {
        // 1) Convert postId to Long
        final var longCommentId = Long.valueOf(commentId);
        final var longUserId = Long.valueOf(userId);

        // 2) Delete comment via service
        final var deleted = loungePostService.deleteComment(longCommentId, longUserId);

        // 3) Retrieve updated page of comment
        final var comments = loungePostService.getCommentsByPostId(deleted.getPostId(), pageable);

        return ResponseEntity.ok(toDTOList(comments));
    }

    // Helper: Change Instant to LocalDate
    private static LocalDate toLocalDate(Instant instant) {
        return instant == null ? null : LocalDate.ofInstant(instant, ZoneId.of("Asia/Seoul"));
    }

    // Helper: Returns whether the resource was modified (updatedAt > createdAt)
    public static String isModified(Instant createdAt, Instant updatedAt) {
        if(!createdAt.equals(updatedAt)) {
            return "수정됨";
        }
        return null;
    }

    /** Domain -> DTO */
    private GetLoungePostCommentResponse toDTO(LoungePostComment comment) {
        return GetLoungePostCommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .content(comment.getContent())
                .createdAt(toLocalDate(comment.getCreatedAt()))
                .editStatus(isModified(comment.getCreatedAt(), comment.getUpdatedAt()))
                .build();
    }

    private List<GetLoungePostCommentResponse> toDTOList(List<LoungePostComment> comments) {
        return comments.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
