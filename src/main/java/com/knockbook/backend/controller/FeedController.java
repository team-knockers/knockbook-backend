package com.knockbook.backend.controller;

import com.knockbook.backend.dto.*;
import com.knockbook.backend.service.FeedService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/feeds")
@RequiredArgsConstructor
@Validated
public class FeedController {
    private final FeedService feedService;

    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{userId}")
    public ResponseEntity<GetFeedPostsResponse> getFeedPosts (
            @PathVariable("userId") String userId,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String after, // last postId
            @RequestParam @Min(1) int size
    ){
        final var uid = Long.parseLong(userId);
        final var afterId = (after == null || after.isBlank()) ? null : Long.parseLong(after);

        final var result = feedService.getFeedPosts(uid, searchKeyword, afterId, size);

        final var feedPosts = result.getFeedPosts().stream()
                .map(p -> FeedPostDTO.builder()
                        .postId(p.getPostId())
                        .userId(p.getUserId())
                        .displayName(p.getDisplayName())
                        .avatarUrl(p.getAvatarUrl())
                        .content(p.getContent())
                        .images(p.getImages())
                        .likesCount(p.getLikesCount())
                        .commentsCount(p.getCommentsCount())
                        .likedByMe(p.getLikedByMe())
                        .createdAt(DateTimeFormatter.ISO_INSTANT.format(p.getCreatedAt()))
                        .build())
                .toList();

        final var body = GetFeedPostsResponse.builder()
                .feedPosts(feedPosts)
                .nextAfter(result.getNextAfter())
                .build();

        return ResponseEntity.ok(body);
    }

    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/profile/{userId}")
    public ResponseEntity<GetFeedProfileResponse> getFeedProfile(
            @PathVariable("userId") String userId,
            @RequestParam(required = false) String after, // last postId
            @RequestParam @Min(1) int size
    ) {
        final var uid = Long.parseLong(userId);
        final var afterId = (after == null || after.isBlank()) ? null : Long.parseLong(after);
        final var result = feedService.getFeedProfile(uid, afterId, size);
        final var body = GetFeedProfileResponse.builder()
                .userId(result.getUserId())
                .displayName(result.getDisplayName())
                .avatarUrl(result.getAvatarUrl())
                .bio(result.getBio())
                .postsCount(result.getPostsCount())
                .profileThumbnails(
                        result.getProfileThumbnails().stream()
                                .map(t -> FeedProfileThumbnailDTO.builder()
                                        .postId(t.getPostId())
                                        .thumbnailUrl(t.getThumbnailUrl())
                                        .build())
                                .toList())
                .nextAfter(result.getNextAfter())
                .build();

        return ResponseEntity.ok(body);
    }

    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/post/{postId}/comments/{userId}")
    public ResponseEntity<GetFeedCommentsResponse> getFeedPostComments(
            @PathVariable("userId") String userId,
            @PathVariable("postId") Long postId
    ) {
        final var uid = Long.parseLong(userId);
        // 서비스 레포지토리 구현 이후 작성 예정
        final var result = feedService.getFeedPostComments(uid, postId);

        final var feedComments = result.getFeedComments().stream()
                .map(c -> FeedCommentDTO.builder()
                        .commentId(c.getCommentId())
                        .userId(c.getUserId())
                        .displayName(c.getDisplayName())
                        .avatarUrl(c.getAvatarUrl())
                        .body(c.getBody())
                        .createdAt(DateTimeFormatter.ISO_INSTANT.format(c.getCreatedAt()))
                        .likedByMe(c.getLikedByMe())
                        .likesCount(c.getLikesCount())
                        .build()
                ).toList();

        final var body = GetFeedCommentsResponse.builder()
                .feedComments(feedComments)
                .postId(result.getPostId())
                .build();

        return ResponseEntity.ok(body);
    }

    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/post/{postId}/{userId}")
    public ResponseEntity<GetFeedResponse> getFeedPostWithComments(
            @PathVariable("postId") Long postId,
            @PathVariable("userId") String userId
    ) {
        final var uid = Long.parseLong(userId);
        final var result = feedService.getFeedPostWithComments(uid, postId);
        final var post = result.getFeedPost();

        final var feedPost = FeedPostDTO.builder()
                .postId(post.getPostId())
                .userId(post.getUserId())
                .displayName(post.getDisplayName())
                .avatarUrl(post.getAvatarUrl())
                .content(post.getContent())
                .images(post.getImages())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .likedByMe(post.getLikedByMe())
                .createdAt(DateTimeFormatter.ISO_INSTANT.format(post.getCreatedAt()))
                .build();

        final var feedComments = result.getFeedComments().stream()
                .map(c -> FeedCommentDTO.builder()
                        .commentId(c.getCommentId())
                        .userId(c.getUserId())
                        .displayName(c.getDisplayName())
                        .avatarUrl(c.getAvatarUrl())
                        .body(c.getBody())
                        .createdAt(DateTimeFormatter.ISO_INSTANT.format(c.getCreatedAt()))
                        .likedByMe(c.getLikedByMe())
                        .likesCount(c.getLikesCount())
                        .build()
                ).toList();

        final var body = GetFeedResponse.builder()
                .feedPost(feedPost)
                .feedComments(feedComments)
                .build();

        return ResponseEntity.ok(body);
    }
}
