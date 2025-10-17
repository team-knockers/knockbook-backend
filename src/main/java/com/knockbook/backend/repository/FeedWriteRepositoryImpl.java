package com.knockbook.backend.repository;

import com.knockbook.backend.domain.FeedComment;
import com.knockbook.backend.entity.FeedCommentEntity;
import com.knockbook.backend.entity.QFeedCommentEntity;
import com.knockbook.backend.entity.QFeedPostEntity;
import com.knockbook.backend.entity.QUserEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedWriteRepositoryImpl implements FeedWriteRepository{

    private final JPAQueryFactory query;
    private final EntityManager em;

    private static final QFeedPostEntity P = QFeedPostEntity.feedPostEntity;
    private static final QFeedCommentEntity C = QFeedCommentEntity.feedCommentEntity;
    private static final QUserEntity U = QUserEntity.userEntity;

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
}
