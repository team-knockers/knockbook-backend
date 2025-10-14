package com.knockbook.backend.repository;

import com.knockbook.backend.domain.FeedPost;
import com.knockbook.backend.domain.FeedPostsResult;
import com.knockbook.backend.entity.QFeedPostEntity;
import com.knockbook.backend.entity.QFeedPostImageEntity;
import com.knockbook.backend.entity.QFeedPostLikeEntity;
import com.knockbook.backend.entity.QUserEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class FeedRepositoryImpl implements FeedRepository {

    private final JPAQueryFactory query;

    private static final QFeedPostEntity P = QFeedPostEntity.feedPostEntity;
    private static final QFeedPostImageEntity I = QFeedPostImageEntity.feedPostImageEntity;
    private static final QFeedPostLikeEntity L = QFeedPostLikeEntity.feedPostLikeEntity;
    private static final QUserEntity U = QUserEntity.userEntity;

    @Override
    public FeedPostsResult findFeedPosts(
            Long userId,
            String searchKeyword,
            Long after,
            int size
    ) {
        // filters
        final var predicate = new BooleanBuilder().and(P.deletedAt.isNull());
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            final var kw = "%" + searchKeyword.trim() + "%";
            predicate.and(P.content.like(kw));
        }

        // cursor createdAt
        final var afterCreatedAt = (after == null) ? null
                : query.select(P.createdAt).from(P)
                .where(P.postId.eq(after), P.deletedAt.isNull())
                .fetchOne();

        // keyset (created_at DESC, post_id DESC)
        if (afterCreatedAt != null) {
            predicate.and(
                    P.createdAt.lt(afterCreatedAt)
                            .or(P.createdAt.eq(afterCreatedAt).and(P.postId.lt(after)))
            );
        }

        // likedByMe via EXISTS
        final var likedByMeExpr = JPAExpressions
                .selectOne()
                .from(L)
                .where(L.postId.eq(P.postId).and(L.userId.eq(userId)))
                .exists();

        // page rows
        final var rows = query
                .select(
                        P.postId, P.userId, P.content, P.likesCount, P.commentsCount, P.createdAt,
                        U.displayName, U.avatarUrl,
                        likedByMeExpr
                )
                .from(P)
                .leftJoin(U).on(U.id.eq(P.userId))
                .where(predicate)
                .orderBy(P.createdAt.desc(), P.postId.desc())
                .limit(size + 1L)
                .fetch();

        // cut to size
        final var hasMore = rows.size() > size;
        final var pageRows = hasMore ? rows.subList(0, size) : rows;

        // no items
        final var postIds = pageRows.stream().map(t -> t.get(P.postId)).toList();
        if (postIds.isEmpty()) {
            return FeedPostsResult.builder().feedPosts(List.of()).nextAfter(null).build();
        }

        // images
        final var imageRows = query
                .select(I.postId, I.imageUrl, I.sortOrder)
                .from(I)
                .where(I.postId.in(postIds))
                .orderBy(I.postId.asc(), I.sortOrder.asc())
                .fetch();

        // images by post
        final var imageMap = new HashMap<Long, List<String>>();
        for (final var r : imageRows) {
            imageMap.computeIfAbsent(r.get(I.postId), k -> new ArrayList<>())
                    .add(r.get(I.imageUrl));
        }

        // map
        final var feedPosts = pageRows.stream().map(t ->
                FeedPost.builder()
                        .postId(String.valueOf(t.get(P.postId)))
                        .userId(String.valueOf(t.get(P.userId)))
                        .displayName(t.get(U.displayName))
                        .avatarUrl(t.get(U.avatarUrl))
                        .content(t.get(P.content))
                        .images(imageMap.getOrDefault(t.get(P.postId), List.of()))
                        .likesCount(t.get(P.likesCount))
                        .commentsCount(t.get(P.commentsCount))
                        .likedByMe(Boolean.TRUE.equals(t.get(likedByMeExpr)))
                        .createdAt(t.get(P.createdAt))
                        .build()
        ).toList();

        // next pointer
        final var nextAfter = hasMore ? feedPosts.get(feedPosts.size() - 1).getPostId() : null;

        return FeedPostsResult.builder()
                .feedPosts(feedPosts)
                .nextAfter(nextAfter)
                .build();
    }
}
