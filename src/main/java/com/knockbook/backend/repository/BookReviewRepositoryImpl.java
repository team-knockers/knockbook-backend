package com.knockbook.backend.repository;

import com.knockbook.backend.domain.BookReview;
import com.knockbook.backend.domain.BookReviewImage;
import com.knockbook.backend.domain.BookReviewStatistic;
import com.knockbook.backend.domain.MemberLifeBookReview;
import com.knockbook.backend.entity.*;
import com.knockbook.backend.exception.CommentNotFoundException;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
@Repository
@RequiredArgsConstructor
public class BookReviewRepositoryImpl implements BookReviewRepository  {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    private static final QBookReviewEntity R = QBookReviewEntity.bookReviewEntity;
    private static final QBookReviewImageEntity I = QBookReviewImageEntity.bookReviewImageEntity;
    private static final QBookReviewLikeEntity L = QBookReviewLikeEntity.bookReviewLikeEntity;
    private static final QUserEntity U = QUserEntity.userEntity;

    @Override
    public BookReview save(BookReview review) {
        final var entity = BookReviewEntity.builder()
                .bookId(review.getBookId())
                .userId(review.getUserId())
                .transactionType(BookReviewEntity.TransactionType.valueOf(review.getTransactionType().name()))
                .body(review.getContent())
                .rating(review.getRating())
                .status(BookReviewEntity.Status.VISIBLE)
                .likesCount(0)
                .build();

        em.persist(entity);
        em.flush();
        em.refresh(entity);

        return BookReview.builder()
                .id(entity.getId())
                .bookId(entity.getBookId())
                .userId(entity.getUserId())
                .transactionType(BookReview.TransactionType.valueOf(entity.getTransactionType().name()))
                .content(entity.getBody())
                .rating(entity.getRating())
                .likesCount(0)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Override
    public BookReviewImage saveImageAndReturnDomain(Long reviewId, String imageUrl, int sortOrder) {
        BookReviewImageEntity entity = BookReviewImageEntity.builder()
                .bookReviewId(reviewId)
                .imageUrl(imageUrl)
                .sortOrder(sortOrder)
                .build();

        em.persist(entity);

        return BookReviewImage.builder()
                .imageUrl(entity.getImageUrl())
                .sortOrder(entity.getSortOrder())
                .build();
    }

    @Override
    public void softDeleteById(Long reviewId, Long userId) {
        final var review = queryFactory.selectFrom(R)
                .where(R.id.eq(reviewId).and(R.deletedAt.isNull()))
                .fetchOne();

        if (review == null) {
            throw new CommentNotFoundException("댓글이 존재하지 않습니다");
        }
        if (!review.getUserId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        final var deletedReview = review.toBuilder()
                .deletedAt(Instant.now())
                .build();

        em.merge(deletedReview);
        em.flush();
    }

    @Override
    public Page<BookReview> findAllBy(Long bookId, Pageable pageable,
                                            String transactionType, Boolean sameMbti, String currentUserMbti) {
        // 0) Define base predicate (visible + not deleted) and add filter (transaction type, same MBTI)
        BooleanExpression predicate = R.bookId.eq(bookId)
                .and(R.status.eq(BookReviewEntity.Status.VISIBLE))
                .and(R.deletedAt.isNull());

        if (transactionType != null && !"all".equalsIgnoreCase(transactionType)) {
            predicate = predicate.and(R.transactionType.eq(
                    BookReviewEntity.TransactionType.valueOf(transactionType.toUpperCase())
            ));
        }

        final var applySameMbti = Boolean.TRUE.equals(sameMbti)
                && currentUserMbti != null && !currentUserMbti.isBlank();

        if (applySameMbti) {
            predicate = predicate.and(U.mbti.eq(currentUserMbti));
        }

        // 1) projection single-query: fetch review + required user columns in one query
        final var reviewWithUserTuples = queryFactory
                .select(R, U.displayName, U.mbti)
                .from(R)
                .leftJoin(U).on(U.id.eq(R.userId))
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .fetch();

        if (reviewWithUserTuples.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0L);
        }

        // 2) Prepare ids and counts (null-safe)
        final var reviewIds = reviewWithUserTuples.stream()
                .map(t -> t.get(R))
                .filter(Objects::nonNull)
                .map(BookReviewEntity::getId)
                .filter(Objects::nonNull)
                .toList();

        final var totalItems = Optional.ofNullable(
                queryFactory.select(R.count())
                        .from(R)
                        .leftJoin(U).on(U.id.eq(R.userId))
                        .where(predicate)
                        .fetchOne()
        ).orElse(0L);

        // 3) Fetch images once for all reviews on page and group by reviewId
        final var imageEntities = queryFactory
                .selectFrom(I)
                .where(I.bookReviewId.in(reviewIds).and(I.deletedAt.isNull()))
                .orderBy(I.bookReviewId.asc(), I.sortOrder.asc())
                .fetch();

        final var imagesByReviewId = imageEntities.stream()
                .collect(Collectors.groupingBy(BookReviewImageEntity::getBookReviewId, LinkedHashMap::new, Collectors.toList()));

        // 4) Map tuples -> domain BookReview preserving original order
        final var content = reviewWithUserTuples.stream()
                .map(t -> {
                    final var re = t.get(R);
                    if (re == null) {
                        return null;
                    }

                    final var images = Optional.ofNullable(imagesByReviewId.get(re.getId()))
                            .orElse(List.of())
                            .stream()
                            .map(img -> BookReviewImage.builder()
                                    .imageUrl(img.getImageUrl())
                                    .sortOrder(img.getSortOrder())
                                    .build())
                            .toList();

                    return BookReview.builder()
                            .id(re.getId())
                            .bookId(re.getBookId())
                            .userId(re.getUserId())
                            .displayName(t.get(U.displayName))
                            .mbti(t.get(U.mbti))
                            .transactionType(BookReview.TransactionType.valueOf(re.getTransactionType().name()))
                            .content(re.getBody())
                            .rating(re.getRating())
                            .likesCount(re.getLikesCount())
                            .imageUrls(images)
                            .createdAt(re.getCreatedAt())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        return new PageImpl<>(content, pageable, totalItems);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookReview> findAllBy(Long userId) {
        final var predicate = R.userId.eq(userId)
                .and(R.status.eq(BookReviewEntity.Status.VISIBLE))
                .and(R.deletedAt.isNull());

        final var reviewWithUserTuples = queryFactory
                .select(R, U.displayName, U.mbti)
                .from(R)
                .leftJoin(U).on(U.id.eq(R.userId))
                .where(predicate)
                .orderBy(R.createdAt.desc(), R.id.desc())
                .fetch();

        if (reviewWithUserTuples.isEmpty()) {
            return List.of();
        }

        final var reviewIds = reviewWithUserTuples.stream()
                .map(t -> t.get(R))
                .filter(Objects::nonNull)
                .map(BookReviewEntity::getId)
                .filter(Objects::nonNull)
                .toList();

        final var imageEntities = queryFactory
                .selectFrom(I)
                .where(I.bookReviewId.in(reviewIds).and(I.deletedAt.isNull()))
                .orderBy(I.bookReviewId.asc(), I.sortOrder.asc())
                .fetch();

        final var imagesByReviewId = imageEntities.stream()
                .collect(Collectors.groupingBy(
                        BookReviewImageEntity::getBookReviewId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return reviewWithUserTuples.stream()
                .map(t -> {
                    final var re = t.get(R);
                    if (re == null) return null;

                    final var images = Optional.ofNullable(imagesByReviewId.get(re.getId()))
                            .orElse(List.of())
                            .stream()
                            .map(img -> BookReviewImage.builder()
                                    .imageUrl(img.getImageUrl())
                                    .sortOrder(img.getSortOrder())
                                    .build())
                            .toList();

                    return BookReview.builder()
                            .id(re.getId())
                            .bookId(re.getBookId())
                            .userId(re.getUserId())
                            .displayName(t.get(U.displayName))
                            .mbti(t.get(U.mbti))
                            .transactionType(BookReview.TransactionType.valueOf(re.getTransactionType().name()))
                            .content(re.getBody())
                            .rating(re.getRating())
                            .likesCount(re.getLikesCount())
                            .imageUrls(images)
                            .createdAt(re.getCreatedAt())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Set<Long> findLikedReviewIdsBy(Long userId, List<Long> reviewIds) {
        if (userId == null || reviewIds == null || reviewIds.isEmpty()) {
            return Collections.emptySet();
        }

        // Find IDs of reviews liked by the given user (for likedByMe check)
        final var likedReviewIds = queryFactory.select(L.bookReviewId)
                .from(L)
                .where(L.bookReviewId.in(reviewIds)
                        .and(L.userId.eq(userId))
                        .and(L.isLiked.eq(true)))
                .fetch();

        return Set.copyOf(likedReviewIds);
    }

    @Override
    @Transactional
    public boolean saveReviewLike(Long userId, Long reviewId) {
        // 1) restore if exists with isLiked = false
        final var updated = queryFactory.update(L)
                .where(L.bookReviewId.eq(reviewId)
                        .and(L.userId.eq(userId))
                        .and(L.isLiked.eq(false)))
                .set(L.isLiked, true)
                .execute();

        if (updated > 0) {
            return true;
        }

        // 2) try insert
        try {
            final var like = BookReviewLikeEntity.builder()
                    .bookReviewId(reviewId)
                    .userId(userId)
                    .isLiked(true)
                    .build();
            em.persist(like);
            em.flush();
            return true;
        } catch (PersistenceException ex) {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteReviewLikeIfExists(Long userId, Long reviewId) {
        final var updated = queryFactory.update(L)
                .where(L.bookReviewId.eq(reviewId)
                        .and(L.userId.eq(userId))
                        .and(L.isLiked.eq(true)))
                .set(L.isLiked, false)
                .execute();

        return updated > 0;
    }

    @Override
    @Transactional
    public void incrementLikeCount(Long reviewId) {
        queryFactory.update(R)
                .where(R.id.eq(reviewId))
                .set(R.likesCount, R.likesCount.add(1))
                .execute();
    }

    @Override
    @Transactional
    public void decrementLikeCount(Long reviewId) {
        queryFactory.update(R)
                .where(R.id.eq(reviewId).and(R.likesCount.gt(0)))
                .set(R.likesCount, R.likesCount.subtract(1))
                .execute();
    }

    @Override
    public boolean existsReviewLike(Long userId, Long reviewId) {
        final var found = queryFactory.selectOne()
                .from(L)
                .where(L.bookReviewId.eq(reviewId).and(L.userId.eq(userId)).and(L.isLiked.eq(true)))
                .fetchFirst();
        return found != null;
    }

    @Override
    public int getLikeCount(Long reviewId) {
        final var likeCount = queryFactory.select(R.likesCount)
                .from(R)
                .where(R.id.eq(reviewId))
                .fetchOne();
        return likeCount == null ? 0 : likeCount;
    }

    @Override
    public BookReviewStatistic findBookReviewStatisticsBy(Long bookId) {
        BooleanExpression predicate = R.bookId.eq(bookId)
                .and(R.status.eq(BookReviewEntity.Status.VISIBLE))
                .and(R.deletedAt.isNull());

        // avg rating (nullable) and total count
        final var avg = queryFactory.select(R.rating.avg())
                .from(R)
                .where(predicate)
                .fetchOne();

        final var total = queryFactory.select(R.count())
                .from(R)
                .where(predicate)
                .fetchOne();

        final var averageRating = avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0;
        final var totalCount = total == null ? 0L : total;

        // counts by score
        final var scoreTuples = queryFactory.select(R.rating, R.count())
                .from(R)
                .where(predicate)
                .groupBy(R.rating)
                .fetch();

        final var scoreMap = scoreTuples.stream()
                .filter(t -> t.get(R.rating) != null)
                .collect(Collectors.toMap(
                        t -> t.get(R.rating),
                        t -> Optional.ofNullable(t.get(R.count())).orElse(0L)
                ));

        final var scoreCounts = IntStream.rangeClosed(1, 5)
                .mapToObj(score -> BookReviewStatistic.ScoreCount.builder()
                        .score(score)
                        .count(scoreMap.getOrDefault(score, 0L))
                        .build())
                .collect(Collectors.toList());

        // mbti counts (join user)
        final var mbtiTuples = queryFactory.select(U.mbti, R.count())
                .from(R)
                .leftJoin(U).on(U.id.eq(R.userId))
                .where(predicate)
                .groupBy(U.mbti)
                .fetch();

        final var mbtiCounts = mbtiTuples.stream()
                .filter(t -> {
                    String mbti = t.get(U.mbti);
                    return mbti != null && !mbti.trim().isEmpty();
                })
                .map(t -> new BookReviewStatistic.MbtiCount(t.get(U.mbti), Optional.ofNullable(t.get(R.count())).orElse(0L)))
                .collect(Collectors.toList());

        return BookReviewStatistic.builder()
                .averageRating(averageRating)
                .totalCount(totalCount)
                .scoreCounts(scoreCounts)
                .mbtiCounts(mbtiCounts)
                .build();
    }

    @Override
    public Optional<MemberLifeBookReview> findRandomFiveStarReview() {
        final var reviewIds = queryFactory
                .select(R.id)
                .from(R)
                .where(R.rating.eq(5)
                        .and(R.status.eq(BookReviewEntity.Status.VISIBLE))
                        .and(R.deletedAt.isNull()))
                .fetch();

        if (reviewIds.isEmpty()) {
            return Optional.empty();
        }

        Long randomId = reviewIds.get(new Random().nextInt(reviewIds.size()));

        BookReviewEntity entity = queryFactory
                .selectFrom(R)
                .where(R.id.eq(randomId))
                .fetchOne();

        if (entity == null) {
            return Optional.empty();
        }

        return Optional.of(MemberLifeBookReview.builder()
                .id(entity.getId())
                .bookId(entity.getBookId())
                .userId(entity.getUserId())
                .content(entity.getBody())
                .build());
    }

    /**
     * Converts Spring Sort into QueryDSL OrderSpecifiers.
     */
    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        PathBuilder<BookReviewEntity> builder = new PathBuilder<>(BookReviewEntity.class, R.getMetadata());

        return sort.stream()
                .map(order -> {
                    final var prop = mapToEntityField(order.getProperty());
                    final var direction = order.isAscending() ? Order.ASC : Order.DESC;

                    if ("createdAt".equals(prop)) {
                        return new OrderSpecifier<>(direction, builder.getDateTime(prop, Instant.class));
                    } else if ("rating".equals(prop) || "likesCount".equals(prop)) {
                        return new OrderSpecifier<>(direction, builder.getNumber(prop, Integer.class));
                    } else {
                        throw new IllegalArgumentException("Invalid sort property: " + prop);
                    }
                })
                .toArray(OrderSpecifier[]::new);
    }

    /**
     * Maps API sort keys (e.g. "likes") to entity fields (e.g. "likesCount").
     */
    private String mapToEntityField(String sortBy) {
        return switch (sortBy) {
            case "likes"     -> "likesCount";
            case "createdAt" -> "createdAt";
            case "rating"    -> "rating";
            default -> throw new IllegalArgumentException("Invalid sortBy value: " + sortBy);
        };
    }

}
