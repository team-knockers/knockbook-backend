package com.knockbook.backend.controller;

import com.knockbook.backend.domain.LoungePost;
import com.knockbook.backend.domain.LoungePostComment;
import com.knockbook.backend.dto.*;
import com.knockbook.backend.service.LoungePostService;
import com.knockbook.backend.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/lounge")
@Validated
public class LoungePostController {

    @Autowired
    private LoungePostService loungePostService;

    @Autowired
    private UserService userService;

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

    // API-LOUNGE-03: Create a comment and return comment DTO
    @PreAuthorize("#userId == authentication.name")
    @PostMapping("/{userId}/{postId}/comments")
    public ResponseEntity<LoungePostCommentDto> createComment(
            @PathVariable("userId") String userId,
            @PathVariable("postId") String postId,
            @RequestBody CreateLoungePostCommentRequest request
    ) {
        // 1) Convert IDs to Long
        final var longPostId = Long.valueOf(postId);
        final var longUserId = Long.valueOf(userId);

        // 2) Create comment via service
        final var comment = loungePostService.createComment(longPostId, longUserId, request.getContent());

        // 4) Map domain object to DTO
        final var response = toDTO(comment);

        // 5) Return response
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    // API-LOUNGE-04: Get multiple comments with pagination (1-based page)
    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{userId}/{postId}/comments")
    public ResponseEntity<GetLoungePostCommentsPageResponse> getCommentsByPost(
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
        final var commentPage = loungePostService.getCommentsByPostId(longPostId, pageable);

        // 4) Convert domain entities to DTO and build page DTO
        final var response = GetLoungePostCommentsPageResponse.builder()
                .comments(commentPage.stream().map(this::toDTO).toList())
                .page(commentPage.getNumber() + 1)          // 0-based -> 1-based
                .size(commentPage.getSize())
                .totalItems((int) commentPage.getTotalElements())
                .totalPages(commentPage.getTotalPages())
                .build();

        // 5) Return response
        return ResponseEntity.ok(response);
    }

    // API-LOUNGE-05: Get a single comment (for edit preview)
    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{userId}/comments/{commentId}")
    public ResponseEntity<LoungePostCommentDto> getCommentById(
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
    public ResponseEntity<LoungePostCommentDto> updateComment(
            @PathVariable("userId") String userId,
            @PathVariable("commentId") String commentId,
            @RequestBody UpdateLoungePostCommentRequest request
    ) {
        // 1) Convert postId to Long
        final var longCommentId = Long.valueOf(commentId);
        final var longUserId = Long.valueOf(userId);

        // 2) Update comment via service
        final var updated = loungePostService.updateComment(longCommentId, longUserId, request.getContent());

        // 3) Map domain object to DTO
        final var response = toDTO(updated);

        // 4) Return response
        return ResponseEntity.ok(response);
    }

    // API-LOUNGE-07: Delete a comment and return the updated page
    @PreAuthorize("#userId == authentication.name")
    @DeleteMapping("/{userId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("userId") String userId,
            @PathVariable("commentId") String commentId
    ) {
        // 1) Convert postId to Long
        final var longCommentId = Long.valueOf(commentId);
        final var longUserId = Long.valueOf(userId);

        // 2) Delete comment via service
        loungePostService.deleteComment(longCommentId, longUserId);

        // 3) Return response
        return ResponseEntity.noContent().build();
    }

    // API-LOUNGE-08: Like post
    @PreAuthorize("#userId == authentication.name")
    @PutMapping("/{userId}/{postId}/likes")
    public ResponseEntity<Void> likePost(
            @PathVariable String userId,
            @PathVariable String postId
    ) {
        loungePostService.likePost(Long.valueOf(userId), Long.valueOf(postId));
        return ResponseEntity.noContent().build();
    }

    // API-LOUNGE-09: Unike post
    @PreAuthorize("#userId == authentication.name")
    @DeleteMapping("/{userId}/{postId}/likes")
    public ResponseEntity<Void> unlikePost(
            @PathVariable String userId,
            @PathVariable String postId
    ) {
        loungePostService.unlikePost(Long.valueOf(userId), Long.valueOf(postId));
        return ResponseEntity.noContent().build();
    }

    // API-LOUNGE-10: Check if a user has liked a post
    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{userId}/{postId}/likes")
    public ResponseEntity<LoungePostLikeStatusResponse> isPostLiked(
            @PathVariable String userId,
            @PathVariable String postId
    ) {
        // 1) Convert postId to Long
        final var longUserId = Long.valueOf(userId);
        final var longPostId = Long.valueOf(postId);

        // 2) Check if the user has liked the post via service
        final var isLiked = loungePostService.isPostLikedByUser(longUserId, longPostId);

        // 3) Wrap the result into response DTO
        final var response = LoungePostLikeStatusResponse.builder()
                .isLiked(isLiked)
                .build();

        // 4) Return response
        return ResponseEntity.ok(response);
    }

    // API-LOUNGE-11: Create a lounge post
    @PreAuthorize("#userId == authentication.name")
    @PostMapping(value = "/{userId}/posts", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<LoungePostCreateResponse> createLoungePost(
            @PathVariable String userId,
            @RequestPart("post") @Valid LoungePostCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        // 1) Request DTO → Domain
        final var post = LoungePost.builder()
                .userId(Long.valueOf(userId))
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .content(request.getContent())
                .status(LoungePost.Status.VISIBLE)
                .likeCount(0)
                .build();

        // 2) Call service to save the post
        final var saved = loungePostService.createPost(post, images);

        // 3) Instant → LocalDateTime
        final var createdAt = toLocalDateTime(saved.getCreatedAt());

        // 4) Create Response DTO
        final var response = LoungePostCreateResponse.builder()
                .id(String.valueOf(saved.getId()))
                .createdAt(createdAt)
                .build();

        // 5) Return response
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // API-LOUNGE-12: Delete a lounge post
    @PreAuthorize("#userId == authentication.name")
    @DeleteMapping("/{userId}/posts/{postId}")
    public ResponseEntity<Void> deleteLoungePost(
            @PathVariable String userId,
            @PathVariable String postId
    ) {
        loungePostService.hardDeletePost(Long.valueOf(postId), Long.valueOf(userId));
        return ResponseEntity.noContent().build();
    }

    // Helper: Change Instant to LocalDate
    private static LocalDate toLocalDate(Instant instant) {
        return instant == null ? null : LocalDate.ofInstant(instant, ZoneId.of("Asia/Seoul"));
    }

    // Helper: Change Instant to LocalDateTime (Asia/Seoul)
    private static LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Seoul"));
    }

    // Helper: Returns whether the resource was modified (updatedAt > createdAt)
    public static String isModified(Instant createdAt, Instant updatedAt) {
        if(!createdAt.equals(updatedAt)) {
            return "수정됨";
        }
        return null;
    }

    /** Domain -> DTO */
    private LoungePostCommentDto toDTO(LoungePostComment comment) {

        String content = comment.getStatus() == LoungePostComment.Status.VISIBLE
                ? comment.getContent()
                : "신고 처리되어 볼 수 없는 글입니다.";

        return LoungePostCommentDto.builder()
                .id(String.valueOf(comment.getId()))
                .postId(String.valueOf(comment.getPostId()))
                .userId(String.valueOf(comment.getUserId()))
                .displayName(comment.getDisplayName())
                .avatarUrl(comment.getAvatarUrl())
                .content(content)
                .createdAt(toLocalDate(comment.getCreatedAt()))
                .editStatus(isModified(comment.getCreatedAt(), comment.getUpdatedAt()))
                .build();
    }

    private List<LoungePostCommentDto> toDTOList(List<LoungePostComment> comments) {
        return comments.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
