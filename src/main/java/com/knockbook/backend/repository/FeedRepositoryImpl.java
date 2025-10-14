package com.knockbook.backend.repository;

import com.knockbook.backend.domain.FeedPost;
import com.knockbook.backend.domain.FeedPostsResult;
import com.knockbook.backend.domain.FeedProfileResult;
import com.knockbook.backend.domain.FeedProfileThumbnail;
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
        // 1) Base filter (alive posts)
        final var predicate = new BooleanBuilder().and(P.deletedAt.isNull());

        // 2) Optional search
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            final var kw = "%" + searchKeyword.trim() + "%";
            predicate.and(P.content.like(kw));
        }

        // 3) Resolve cursor createdAt (if after exists)
        final var afterCreatedAt = (after == null) ? null
                : query.select(P.createdAt).from(P)
                .where(P.postId.eq(after), P.deletedAt.isNull())
                .fetchOne();

        // 4) Keyset window: (created_at DESC, post_id DESC)
        if (afterCreatedAt != null) {
            predicate.and(
                    P.createdAt.lt(afterCreatedAt)
                            .or(P.createdAt.eq(afterCreatedAt).and(P.postId.lt(after)))
            );
        }

        // 5) likedByMe via EXISTS
        final var likedByMeExpr = JPAExpressions
                .selectOne()
                .from(L)
                .where(L.postId.eq(P.postId).and(L.userId.eq(userId)))
                .exists();

        // 6) Page rows (size+1 to detect hasMore)
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

        // 7) Trim to page
        final var hasMore = rows.size() > size;
        final var pageRows = hasMore ? rows.subList(0, size) : rows;

        // 8) Early return when empty
        final var postIds = pageRows.stream().map(t -> t.get(P.postId)).toList();
        if (postIds.isEmpty()) {
            return FeedPostsResult.builder().feedPosts(List.of()).nextAfter(null).build();
        }

        // 9) Fetch images (ordered by postId, sortOrder)
        final var imageRows = query
                .select(I.postId, I.imageUrl, I.sortOrder)
                .from(I)
                .where(I.postId.in(postIds))
                .orderBy(I.postId.asc(), I.sortOrder.asc())
                .fetch();

        // 10) Build image map (postId -> url list)
        final var imageMap = new HashMap<Long, List<String>>();
        for (final var r : imageRows) {
            imageMap.computeIfAbsent(r.get(I.postId), k -> new ArrayList<>())
                    .add(r.get(I.imageUrl));
        }

        // 11) Map tuples -> domain
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

        // 12) Next cursor
        final var nextAfter = hasMore ? feedPosts.get(feedPosts.size() - 1).getPostId() : null;

        // 13) Assemble result
        final var result = FeedPostsResult.builder()
                .feedPosts(feedPosts)
                .nextAfter(nextAfter)
                .build();
        return result;
    }

    @Override
    public FeedProfileResult findFeedProfile(
            Long userId,
            Long after,
            int size
    ) {
        // 1) Read user profile
        final var userTuple = query
                .select(U.displayName, U.avatarUrl, U.bio)
                .from(U)
                .where(U.id.eq(userId))
                .fetchOne();

        // 2) Count posts by user
        final var postsCount = query
                .select(P.postId.count())
                .from(P)
                .where(P.userId.eq(userId), P.deletedAt.isNull())
                .fetchOne();

        // 3) Resolve cursor createdAt (if after given)
        final var afterCreatedAt = (after == null) ? null
                : query.select(P.createdAt)
                .from(P)
                .where(P.postId.eq(after), P.deletedAt.isNull())
                .fetchOne();

        // 4) Page predicate = keyset window
        final var pagePredicate = new BooleanBuilder()
                .and(P.deletedAt.isNull())
                .and(P.userId.eq(userId));
        if (afterCreatedAt != null) {
            pagePredicate.and(
                    P.createdAt.lt(afterCreatedAt)
                            .or(P.createdAt.eq(afterCreatedAt).and(P.postId.lt(after)))
            );
        }

        // 5) Fetch thumbnails (only sort_order=1), ordered by keyset
        final var rows = query
                .select(P.postId, I.imageUrl)
                .from(P)
                .join(I).on(I.postId.eq(P.postId).and(I.sortOrder.eq(1)))
                .where(pagePredicate)
                .orderBy(P.createdAt.desc(), P.postId.desc())
                .limit(size + 1L)
                .fetch();

        // 6) Trim to the requested size and map to domain
        final var hasMore = rows.size() > size;
        final var page = hasMore ? rows.subList(0, size) : rows;

        final var thumbnails = page.stream()
                .map(t -> FeedProfileThumbnail.builder()
                        .postId(String.valueOf(t.get(P.postId)))
                        .thumbnailUrl(t.get(I.imageUrl))
                        .build())
                .toList();

        // 6) Trim to the requested size and map to domain
        final var nextAfter = (hasMore && !thumbnails.isEmpty())
                ? thumbnails.get(thumbnails.size() - 1).getPostId()
                : null;

        // 8) Assemble profile result
        final var result = FeedProfileResult.builder()
                .userId(String.valueOf(userId))
                .displayName(userTuple == null ? null : userTuple.get(U.displayName))
                .avatarUrl(userTuple == null ? null : userTuple.get(U.avatarUrl))
                .bio(userTuple == null ? null : userTuple.get(U.bio))
                .postsCount(postsCount == null ? 0L : postsCount) // Long
                .profileThumbnails(thumbnails)
                .nextAfter(nextAfter)
                .build();
        return result;
    }
}
