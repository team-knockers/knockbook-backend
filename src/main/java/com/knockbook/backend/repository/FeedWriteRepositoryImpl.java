package com.knockbook.backend.repository;

import com.knockbook.backend.domain.FeedComment;
import com.knockbook.backend.domain.FeedProfileThumbnail;
import com.knockbook.backend.entity.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FeedWriteRepositoryImpl implements FeedWriteRepository{

    private final JPAQueryFactory query;
    private final EntityManager em;

    private static final QFeedPostEntity P = QFeedPostEntity.feedPostEntity;
    private static final QUserEntity U = QUserEntity.userEntity;
    private static final QFeedCommentEntity C = QFeedCommentEntity.feedCommentEntity;

    @Override
    public FeedComment insertComment (
            Long postId,
            Long userId,
            String commentBody
    ) {
        final var comment = FeedCommentEntity.builder()
                .postId(postId)
                .userId(userId)
                .body(commentBody)
                .build();

        em.persist(comment);
        em.flush();
        em.refresh(comment);

        final var user = query.select(U.displayName, U.avatarUrl)
                .from(U)
                .where(U.id.eq(userId))
                .fetchOne();

        final var feedComment = FeedComment.builder()
                .commentId(String.valueOf(comment.getCommentId()))
                .userId(String.valueOf(userId))
                .displayName(user.get(U.displayName))
                .avatarUrl(user.get(U.avatarUrl))
                .body(comment.getBody())
                .createdAt(comment.getCreatedAt())
                .likedByMe(false)
                .likesCount(0)
                .build();

        return feedComment;
    }

    @Override
    public void incrementPostCommentsCount (
            Long postId
    ) {
        query.update(P)
                .set(P.commentsCount, P.commentsCount.add(1))
                .where(P.postId.eq(postId))
                .execute();
    }

    @Override
    public FeedProfileThumbnail insertPost (
            Long userId,
            String content,
            List<String> imageUrls
    ) {
        final var post = FeedPostEntity.builder()
                .userId(userId)
                .content(content)
                .build();

        em.persist(post);
        em.flush();

        final var postId = post.getPostId();

        int order = 1;
        for (String url : imageUrls) {
            if (url == null || url.isBlank()) continue;

            final var img = FeedPostImageEntity.builder()
                    .postId(postId)
                    .imageUrl(url)
                    .sortOrder(order++)
                    .build();

            em.persist(img);
        }
        em.flush();

        final String thumbUrl = imageUrls.get(0);

        return FeedProfileThumbnail.builder()
                .postId(String.valueOf(postId))
                .thumbnailUrl(thumbUrl)
                .build();
    }

    @Override
    public Optional<Long> findPostIdByCommentIdAndUserId (
            Long commentId,
            Long userId
    ) {
        final var postId = query
                .select(C.postId)
                .from(C)
                .where(C.commentId.eq(commentId).and(C.userId.eq(userId)))
                .fetchOne();

        return Optional.ofNullable(postId);
    }

    @Override
    public long deleteCommentByIdAndUserId (
            Long commentId,
            Long userId
    ) {
        final var affected = query.delete(C)
                .where(C.commentId.eq(commentId).and(C.userId.eq(userId)))
                .execute();

        return affected;
    }

    @Override
    public void decrementPostCommentsCount (
            Long postId
    ) {
        query.update(P)
                .set(P.commentsCount, P.commentsCount.add(-1))
                .where(P.postId.eq(postId))
                .execute();
    }

    @Override
    public long deletePostByIdAndUserId (
            Long postId,
            Long userId
    ) {
        final var affected = query.delete(P)
                .where(P.postId.eq(postId).and(P.userId.eq(userId)))
                .execute();

        return affected;
    }
}
