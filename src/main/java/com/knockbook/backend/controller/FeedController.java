package com.knockbook.backend.controller;

import com.knockbook.backend.dto.*;
import com.knockbook.backend.service.FeedService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/feeds")
@RequiredArgsConstructor
@Validated
public class FeedController {
    private final FeedService feedService;

    // Read
    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{userId}")
    public ResponseEntity<GetFeedPostsResponse> getFeedPosts (
            @PathVariable("userId") String userId,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String after, // last postId
            @RequestParam @Min(1) int size,
            @RequestParam(required=false) String mbti
    ){
        final var uid = Long.parseLong(userId);
        final var afterId = (after == null || after.isBlank()) ? null : Long.parseLong(after);

        final var result = feedService.getFeedPosts(uid, searchKeyword, afterId, size, mbti);

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
                        .savedByMe(p.getSavedByMe())
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
    @GetMapping("/profile/post/{userId}")
    public ResponseEntity<GetFeedProfileResponse> getProfilePostThumbnails(
            @PathVariable("userId") String userId,
            @RequestParam(required = false) String after, // last postId
            @RequestParam @Min(1) int size
    ) {
        final var uid = Long.parseLong(userId);
        final var afterId = (after == null || after.isBlank()) ? null : Long.parseLong(after);
        final var result = feedService.getProfilePostThumbnails(uid, afterId, size);
        final var body = GetFeedProfileResponse.builder()
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
    @GetMapping("/profile/saved/{userId}")
    public ResponseEntity<GetFeedProfileResponse> getProfileSavedThumbnails(
            @PathVariable("userId") String userId,
            @RequestParam(required = false) String after, // last postId
            @RequestParam @Min(1) int size
    ) {
        final var uid = Long.parseLong(userId);
        final var afterId = (after == null || after.isBlank()) ? null : Long.parseLong(after);
        final var result = feedService.getProfileSavedThumbnails(uid, afterId, size);
        final var body = GetFeedProfileResponse.builder()
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

    // Save
    @PreAuthorize("#userId == authentication.name")
    @PutMapping("/post/{postId}/saves/{userId}")
    public ResponseEntity<Void> savePost(
            @PathVariable("postId") Long postId,
            @PathVariable("userId") String userId
    ) {
        final var uid = Long.parseLong(userId);
        feedService.savePost(postId, uid);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("#userId == authentication.name")
    @DeleteMapping("/post/{postId}/saves/{userId}")
    public ResponseEntity<Void> unsavePost(
            @PathVariable("postId") Long postId,
            @PathVariable("userId") String userId
    ) {
        final var uid = Long.parseLong(userId);
        feedService.unsavePost(postId, uid);

        return ResponseEntity.noContent().build();
    }

    // Like
    @PreAuthorize("#userId == authentication.name")
    @PutMapping("/post/{postId}/likes/{userId}")
    public ResponseEntity<Void> likePost(
            @PathVariable("postId") Long postId,
            @PathVariable("userId") String userId
    ) {
        final var uid = Long.parseLong(userId);
        feedService.likePost(postId, uid);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("#userId == authentication.name")
    @DeleteMapping("/post/{postId}/likes/{userId}")
    public ResponseEntity<Void> unlikePost(
            @PathVariable("postId") Long postId,
            @PathVariable("userId") String userId
    ) {
        final var uid = Long.parseLong(userId);
        feedService.unlikePost(postId, uid);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("#userId == authentication.name")
    @PutMapping("/comment/{commentId}/likes/{userId}")
    public ResponseEntity<Void> likeComment(
            @PathVariable("commentId") Long commentId,
            @PathVariable("userId") String userId
    ) {
        final var uid = Long.parseLong(userId);
        feedService.likeComment(commentId, uid);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("#userId == authentication.name")
    @DeleteMapping("/comment/{commentId}/likes/{userId}")
    public ResponseEntity<Void> unlikeComment(
            @PathVariable("commentId") Long commentId,
            @PathVariable("userId") String userId
    ) {
        final var uid = Long.parseLong(userId);
        feedService.unlikeComment(commentId, uid);

        return ResponseEntity.noContent().build();
    }

    // Write

    @PreAuthorize("#userId == authentication.name")
    @PostMapping("/comment/{postId}/{userId}")
    public ResponseEntity<FeedCommentDTO> createComment(
            @PathVariable("postId") Long postId,
            @PathVariable("userId") String userId,
            @RequestBody @Valid CreateFeedCommentRequest req
            ) {
        final var uid = Long.parseLong(userId);
        final var feedComment = feedService.createComment(postId, uid, req);
        final var comment = FeedCommentDTO.builder()
                .commentId(feedComment.getCommentId())
                .userId(feedComment.getUserId())
                .displayName(feedComment.getDisplayName())
                .avatarUrl(feedComment.getAvatarUrl())
                .body(feedComment.getBody())
                .createdAt(feedComment.getCreatedAt().toString())
                .likedByMe(feedComment.getLikedByMe())
                .likesCount(feedComment.getLikesCount())
                .build();

        final var location = URI.create(
                "/feeds/comment/%s".formatted(comment.getCommentId())
        );

        return ResponseEntity.created(location).body(comment);
    }

    @PreAuthorize("#userId == authentication.name")
    @PostMapping(
            path = "/post/{userId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FeedProfileThumbnailDTO> createPost(
            @PathVariable("userId") String userId,
            @RequestParam(name = "content") String content,
            @RequestParam(name = "files") List<MultipartFile> files
    ) {
        final var uid = Long.parseLong(userId);
        final var thumbnail = feedService.createPost(uid, content, files);

        final var location = URI.create("/feeds/post/%s".formatted(thumbnail.getPostId()));
        final var dto = FeedProfileThumbnailDTO.builder()
                .postId(thumbnail.getPostId())
                .thumbnailUrl(thumbnail.getThumbnailUrl())
                .build();

        return ResponseEntity.created(location).body(dto);
    }

    @PreAuthorize("#userId == authentication.name")
    @DeleteMapping("/comment/{commentId}/{userId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @PathVariable String userId
    ) {
        final var uid = Long.parseLong(userId);
        feedService.deleteComment(commentId, uid);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("#userId == authentication.name")
    @DeleteMapping("/post/{postId}/{userId}")
    public ResponseEntity<Void> deletePost(
        @PathVariable Long postId,
        @PathVariable String userId
    ) {
        final var uid = Long.parseLong(userId);
        feedService.deletePost(postId, uid);

        return ResponseEntity.noContent().build();
    }
}
