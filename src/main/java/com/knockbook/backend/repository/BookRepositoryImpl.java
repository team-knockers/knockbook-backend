package com.knockbook.backend.repository;

import com.knockbook.backend.domain.Book;
import com.knockbook.backend.entity.*;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Book> findByCategory(
            String categoryCodeName, String subcategoryCodeName, Pageable pageable,
            String sortBy, String order, Integer minPrice, Integer maxPrice) {

        // 0) 엔티티 조회
        final var book = QBookEntity.bookEntity;
        final var category = QBookCategoryEntity.bookCategoryEntity;
        final var subcategory = QBookSubcategoryEntity.bookSubcategoryEntity;

        // 1) 조인 조건(on)
        BooleanExpression onCategoryJoin = book.bookCategoryId.eq(category.id);
        BooleanExpression onSubcategoryJoin = book.bookSubcategoryId.eq(subcategory.id);

        // 2) 필터 조건(where)
        BooleanExpression predicate = book.isNotNull(); // 기본 조건

        if (categoryCodeName != null) {
            predicate = predicate.and(
                    book.bookCategoryId.eq(category.id)
                            .and(category.categoryCodeName.eq(categoryCodeName))
            );
        }

        if (subcategoryCodeName != null) {
            predicate = predicate.and(
                    book.bookSubcategoryId.eq(subcategory.id)
                            .and(subcategory.subcategoryCodeName.eq(subcategoryCodeName))
            );
        }
        if (minPrice != null) {
            predicate = predicate.and(book.discountedPurchaseAmount.goe(minPrice));
        }
        if (maxPrice != null) {
            predicate = predicate.and(book.discountedPurchaseAmount.loe(maxPrice));
        }

        // 3) 동적 정렬 OrderSpecifier 생성
        PathBuilder<BookEntity> builder =
                new PathBuilder<>(BookEntity.class, book.getMetadata());
        Order direction =
                "desc".equalsIgnoreCase(order) ? Order.DESC : Order.ASC;
        String prop = mapToEntityField(sortBy);
        OrderSpecifier<?> orderSpec;
        if ("publishedAt".equals(prop)) {
            DatePath<LocalDate> datePath =
                    builder.getDate(prop, LocalDate.class);
            orderSpec = new OrderSpecifier<>(direction, datePath);
        } else {
            NumberPath<Integer> numPath =
                    builder.getNumber(prop, Integer.class);
            orderSpec = new OrderSpecifier<>(direction, numPath);
        }

        // 3) 페이징된 컨텐츠 조회 -> 리스트화
        List<BookEntity> entities = queryFactory
                .select(book)
                .from(book)
                .innerJoin(category).on(onCategoryJoin)
                .innerJoin(subcategory).on(onSubcategoryJoin)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpec)
                .fetch();

        // 4) 전체 건수(count) 조회
        Long totalItems = queryFactory
                .select(book.count())
                .from(book)
                .innerJoin(category).on(onCategoryJoin)
                .innerJoin(subcategory).on(onSubcategoryJoin)
                .where(predicate)
                .fetchOne();

        if (totalItems == null || totalItems == 0L) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 5) 엔티티 → 도메인 매핑
        List<Book> content = entities.stream()
                .map(BookEntityMapper::toDomain)
                .toList();

        // 6) Page 구현체 반환
        return new PageImpl<>(content, pageable, totalItems);
    }

    /**
     * Spring Data Pageable의 Sort 정보를 QueryDSL OrderSpecifier로 변환
     */
    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort, QBookEntity book) {
        PathBuilder<BookEntity> builder =
                new PathBuilder<>(BookEntity.class, book.getMetadata());

        return sort.stream()
                .map(order -> {
                    String prop = mapToEntityField(order.getProperty());
                    Order direction = order.isAscending() ? Order.ASC : Order.DESC;

                    // date-only 필드(published_at) 처리
                    if ("publishedAt".equals(prop)) {
                        DatePath<LocalDate> datePath =
                                builder.getDate(prop, LocalDate.class);
                        return new OrderSpecifier<>(direction, datePath);
                    }

                    // 그 외 숫자 필드(view_count, sales_count, ...)
                    NumberPath<Integer> numPath =
                            builder.getNumber(prop, Integer.class);
                    return new OrderSpecifier<>(direction, numPath);
                })
                .toArray(OrderSpecifier[]::new);
    }

    /**
     * 검색정보 매핑
     */
    private String mapToEntityField(String prop) {
        return switch (prop) {
            case "views"     -> "viewCount";
            case "sales"     -> "salesCount";
            case "rentals"   -> "rentalCount";
            case "price"     -> "discountedPurchaseAmount";
            case "published" -> "publishedAt";
            default          -> "publishedAt";  // 기본값: publishedAt
        };
    }

}
