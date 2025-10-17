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
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/lounge")
@Validated
public class LoungePostController {

    @Autowired
    private LoungePostService loungePostService;

    // API-LOUNGE-01
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

    // API-LOUNGE-02
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

    // API-LOUNGE-03 댓글 작성 (완료 후 반영된 페이지 반환)
    @PreAuthorize("#userId == authentication.name")
    @PostMapping("/{userId}/{postId}/comments")
    public ResponseEntity<List<LoungePostCommentDTO>> createComment(
            @PathVariable("userId") String userId,
            @PathVariable("postId") String postId,
            @RequestBody String content,
            @PageableDefault(page = 0, size = 20, sort = "createdAt") Pageable pageable
    ) {
        // 1) Convert input ID (String -> Long)
        final var longPostId = Long.valueOf(postId);
        final var longUserId = Long.valueOf(userId);

        // 2) Retrieve paged LoungePostSummary from domain
        final var comment = loungePostService.createComment(longPostId, longUserId, content);

        // 3) 생성후 바로 갱신할 단위 댓글 조회
        final List<LoungePostComment> comments = loungePostService.getCommentsByPostId(longPostId, pageable);

        // 4) 결과값 return
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toDTOList(comments));
    }

    // API-LOUNGE-04 댓글 여러 건 조회 (페이지네이션)
    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{userId}/{postId}/comments")
    public ResponseEntity<List<LoungePostCommentDTO>> getCommentsByPost(
            @PathVariable("userId") String userId,
            @PathVariable("postId") String postId,
            @RequestParam(defaultValue = "1") @Min(1) int page,  // 1-based 입력
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        final var zeroBasedPage = page - 1;
        final var pageable = PageRequest.of(zeroBasedPage, size, Sort.by("createdAt").descending());

        final var longPostId = Long.valueOf(postId);
        final var comments = loungePostService.getCommentsByPostId(longPostId, pageable);

        return ResponseEntity.ok(toDTOList(comments));
    }

    // API-LOUNGE-05 댓글 단건 조회 (수정 작업 전 글자 띄우는 용도)
    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{userId}/comments/{commentId}")
    public ResponseEntity<LoungePostCommentDTO> getCommentById(
            @PathVariable("userId") String userId,
            @PathVariable("commentId") String commentId
    ) {
        final var longCommentId = Long.valueOf(commentId);
        final var comment = loungePostService.getComment(longCommentId);

        return ResponseEntity.ok(toDTO(comment));
    }

    // API-LOUNGE-06 댓글 수정 (완료 후 반영된 페이지 반환)
    @PreAuthorize("#userId == authentication.name")
    @PutMapping("/{userId}/comments/{commentId}")
    public ResponseEntity<List<LoungePostCommentDTO>> updateComment(
            @PathVariable("userId") String userId,
            @PathVariable("commentId") String commentId,
            @RequestBody String content,
            @PageableDefault(page = 0, size = 20, sort = "createdAt") Pageable pageable
    ) {
        final var longCommentId = Long.valueOf(commentId);
        final var longUserId = Long.valueOf(userId);

        final var updated = loungePostService.updateComment(longCommentId, longUserId, content);

        // 업데이트 후 같은 게시글의 페이지 단위 댓글 반환
        final var comments = loungePostService.getCommentsByPostId(updated.getPostId(), pageable);

        return ResponseEntity.ok(toDTOList(comments));
    }

    // API-LOUNGE-07 댓글 삭제 (완료 후 반영된 페이지 반환)
    @PreAuthorize("#userId == authentication.name")
    @DeleteMapping("/{userId}/comments/{commentId}")
    public ResponseEntity<List<LoungePostCommentDTO>> deleteComment(
            @PathVariable("userId") String userId,
            @PathVariable("commentId") String commentId,
            @PageableDefault(page = 0, size = 20, sort = "createdAt") Pageable pageable
    ) {
        final var longCommentId = Long.valueOf(commentId);
        final var longUserId = Long.valueOf(userId);

        final var deleted = loungePostService.deleteComment(longCommentId, longUserId);

        final var comments = loungePostService.getCommentsByPostId(deleted.getPostId(), pageable);

        return ResponseEntity.ok(toDTOList(comments));
    }

    // Helper: Change Instant to LocalDate
    private static LocalDate toLocalDate(Instant instant) {
        return instant == null ? null : LocalDate.ofInstant(instant, ZoneId.of("Asia/Seoul"));
    }

    /** Domain -> DTO 변환 */
    private LoungePostCommentDTO toDTO(LoungePostComment comment) {
        return LoungePostCommentDTO.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt().toString())
                .updatedAt(comment.getUpdatedAt().toString())
                .build();
    }

    private List<LoungePostCommentDTO> toDTOList(List<LoungePostComment> comments) {
        return comments.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
