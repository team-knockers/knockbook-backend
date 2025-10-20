package com.knockbook.backend.service;

import com.knockbook.backend.component.ImgbbUploader;
import com.knockbook.backend.domain.*;
import com.knockbook.backend.dto.CreateFeedCommentRequest;
import com.knockbook.backend.exception.AttachmentLimitExceededException;
import com.knockbook.backend.repository.FeedLikeRepository;
import com.knockbook.backend.repository.FeedReadRepository;
import com.knockbook.backend.repository.FeedSaveRepository;
import com.knockbook.backend.repository.FeedWriteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.security.access.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedReadRepository feedReadRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final FeedSaveRepository feedSaveRepository;
    private final FeedWriteRepository feedWriteRepository;

    private static final int MAX_NUM_FILES = 3;
    private final ImgbbUploader imgbb;

    public FeedPostsResult getFeedPosts(
            Long userId,
            String searchKeyword,
            Long after,
            int size,
            String mbti
    ) {
        return feedReadRepository.findFeedPosts(userId, searchKeyword, after, size, mbti);
    }

    public FeedProfileResult getFeedProfile(
            Long userId,
            Long after,
            int size
    ) {
        return feedReadRepository.findFeedProfile(userId, after, size);
    }

    public FeedCommentsResult getFeedPostComments(
            Long userId,
            Long postId
    ) {
        return feedReadRepository.findFeedPostComments(userId, postId);
    }

    public FeedResult getFeedPostWithComments(
            Long userId,
            Long postId
    ) {
        return feedReadRepository.findFeedPostWithComments(userId, postId);
    }

    @Transactional
    public void savePost(
            Long postId,
            Long userId
    ) {
        feedSaveRepository.insertPostSaveIfAbsent(postId, userId);
    }

    @Transactional
    public void unsavePost(
            Long postId,
            Long userId
    ) {
        feedSaveRepository.deletePostSaveIfPresent(postId, userId);
    }

    @Transactional
    public void likePost(
            Long postId,
            Long userId
    ) {
        if (feedLikeRepository.insertPostLikeIfAbsent(postId, userId)) {
            feedLikeRepository.incrementPostLikesCount(postId);
        }
    }

    @Transactional
    public void unlikePost(
            Long postId,
            Long userId
    ) {
        if(feedLikeRepository.deletePostLikeIfPresent(postId, userId)) {
            feedLikeRepository.decrementPostLikesCount(postId);
        }
    }

    @Transactional
    public void likeComment (
            Long commentId,
            Long userId
    ) {
        if(feedLikeRepository.insertCommentLikeIfAbsent(commentId, userId)) {
            feedLikeRepository.incrementCommentLikesCount(commentId);
        }
    }

    @Transactional
    public void unlikeComment (
            Long commentId,
            Long userId
    ) {
        if(feedLikeRepository.deleteCommentLikeIfPresent(commentId, userId)) {
            feedLikeRepository.decrementCommentLikesCount(commentId);
        }
    }

    @Transactional
    public FeedComment createComment (
            Long postId,
            Long userId,
            CreateFeedCommentRequest req
    ) {
        final var commentBody = req.getCommentBody().trim();
        final var feedComment = feedWriteRepository.insertComment(postId, userId, commentBody);
        feedWriteRepository.incrementPostCommentsCount(postId);

        return feedComment;
    }

    @Transactional
    public FeedProfileThumbnail createPost (
            Long userId,
            String content,
            List<MultipartFile> files
    ) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is required");
        }

        if (files != null && !files.isEmpty()) {
            if (files.size() > MAX_NUM_FILES) {
                throw new AttachmentLimitExceededException(MAX_NUM_FILES, files.size());
            }
        }

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("at least 1 image is required");
        }

        final var safeFiles = files.stream()
                .filter(f -> f != null && !f.isEmpty())
                .toList();
        if (safeFiles.isEmpty()) {
            throw new IllegalArgumentException("all files are empty");
        }

        final var imageUrls = new ArrayList<String>();
        for (final var file : safeFiles) {
            final var url = imgbb.upload(file);
            imageUrls.add(url);
        }

        return feedWriteRepository.insertPost(userId, content, imageUrls);
    }

    @Transactional
    public void deleteComment (
            Long commentId,
            Long userId
    ) {
        final var postId = feedWriteRepository
                .findPostIdByCommentIdAndUserId(commentId, userId)
                .orElseThrow(() -> new AccessDeniedException("Not owner or not found"));

        final var affected = feedWriteRepository.deleteCommentByIdAndUserId(commentId, userId);

        if (affected > 0) {
            feedWriteRepository.decrementPostCommentsCount(postId);
        }
    }

    @Transactional
    public void deletePost(
        Long postId,
        Long userId
    ) {
        final var affected = feedWriteRepository.deletePostByIdAndUserId(postId, userId);

        if (affected == 0) {
            throw new AccessDeniedException("Not owner or not found");
        }
    }
}
