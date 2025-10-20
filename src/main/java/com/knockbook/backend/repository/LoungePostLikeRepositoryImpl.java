package com.knockbook.backend.repository;

import com.knockbook.backend.entity.LoungePostLikeEntity;
import com.knockbook.backend.entity.QLoungePostEntity;
import com.knockbook.backend.entity.QLoungePostLikeEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LoungePostLikeRepositoryImpl implements LoungePostLikeRepository {

    private final EntityManager em;
    private final JPAQueryFactory query;

    private static final QLoungePostLikeEntity L = QLoungePostLikeEntity.loungePostLikeEntity;
    private static final QLoungePostEntity P = QLoungePostEntity.loungePostEntity;

    @Override
    public boolean savePostLike(Long userId, Long postId) {
        // Check if the like already exists
        final var exists = query.selectOne()
                .from(L)
                .where(L.postId.eq(postId)
                        .and(L.userId.eq(userId))
                        .and(L.isLiked.eq(true)))
                .fetchFirst();

        if (exists != null) {
            return false;
        }

        // Restore the like if it was previously canceled (isLiked=false)
        final var restored = query.update(L)
                .where(L.postId.eq(postId)
                        .and(L.userId.eq(userId))
                        .and(L.isLiked.eq(false)))
                .set(L.isLiked, true)
                .execute();

        if (restored > 0) {
            return true;
        }

        // Insert a new like record if it doesn't exist
        final var like = LoungePostLikeEntity.builder()
                .postId(postId)
                .userId(userId)
                .isLiked(true)
                .build();
        em.persist(like);
        em.flush();
        return true;
    }

    @Override
    public boolean deletePostLikeIfExists(Long userId, Long postId) {
        // If the like is already canceled, ignore the request
        final var existsFalse = query.selectOne()
                .from(L)
                .where(L.postId.eq(postId)
                        .and(L.userId.eq(userId))
                        .and(L.isLiked.eq(false)))
                .fetchFirst();

        if (existsFalse != null) {
            return false;
        }

        // Only cancel the like if it currently exists
        final var updated = query.update(L)
                .where(L.postId.eq(postId)
                        .and(L.userId.eq(userId))
                        .and(L.isLiked.eq(true)))
                .set(L.isLiked, false)
                .execute();

        // If no row was affected (like record doesn't exist), silently ignore
        return updated != 0;
    }

    @Override
    public void incrementLikeCount(Long postId) {
        query.update(P)
                .where(P.id.eq(postId))
                .set(P.likeCount, P.likeCount.add(1))
                .execute();
    }

    @Override
    public void decrementLikeCount(Long postId) {
        query.update(P)
                .where(P.id.eq(postId).and(P.likeCount.gt(0)))
                .set(P.likeCount, P.likeCount.subtract(1))
                .execute();
    }

    @Override
    public boolean existsByUserIdAndPostIdAndIsLikedTrue(Long userId, Long postId) {
        final var exists = query.selectOne()
                .from(L)
                .where(L.postId.eq(postId)
                        .and(L.userId.eq(userId))
                        .and(L.isLiked.eq(true)))
                .fetchFirst();
        return exists != null;
    }
}
