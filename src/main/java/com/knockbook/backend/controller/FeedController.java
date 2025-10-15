package com.knockbook.backend.controller;

import com.knockbook.backend.dto.FeedPostDTO;
import com.knockbook.backend.dto.FeedProfileThumbnailDTO;
import com.knockbook.backend.dto.GetFeedPostsResponse;
import com.knockbook.backend.dto.GetFeedProfileResponse;
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
    public ResponseEntity<GetFeedProfileResponse> getProfilePosts(
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
}
