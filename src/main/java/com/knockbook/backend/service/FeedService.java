package com.knockbook.backend.service;

import com.knockbook.backend.domain.FeedCommentsResult;
import com.knockbook.backend.domain.FeedPostsResult;
import com.knockbook.backend.domain.FeedProfileResult;
import com.knockbook.backend.domain.FeedResult;
import com.knockbook.backend.dto.CreateFeedCommentRequest;
import com.knockbook.backend.repository.FeedLikeRepository;
import com.knockbook.backend.repository.FeedReadRepository;
import com.knockbook.backend.repository.FeedWriteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedReadRepository feedReadRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final FeedWriteRepository feedWriteRepository;

    public FeedPostsResult getFeedPosts(
            Long userId,
            String searchKeyword,
            Long after,
            int size
    ) {
        return feedReadRepository.findFeedPosts(userId, searchKeyword, after, size);
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
    public Long createComment (
            Long postId,
            Long userId,
            CreateFeedCommentRequest req
    ) {
        final var commentBody = req.getCommentBody().trim();
        final var commentId = feedWriteRepository.insertComment(postId, userId, commentBody);
        feedWriteRepository.incrementPostCommentsCount(postId);

        return commentId;
    }
}
