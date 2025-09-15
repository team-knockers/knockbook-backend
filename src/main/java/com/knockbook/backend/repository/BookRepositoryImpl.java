package com.knockbook.backend.repository;

import com.knockbook.backend.domain.Book;
import com.knockbook.backend.domain.BookSummary;
import com.knockbook.backend.entity.*;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.knockbook.backend.entity.BookEntityMapper.toDomain;

@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    @Override
    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(em.find(BookEntity.class, id))
                .map(BookEntityMapper::toDomain);
    }

    @Override
    public Page<BookSummary> findBooksByCondition(
            String categoryCodeName, String subcategoryCodeName, Pageable pageable, String searchBy,
            String searchKeyword, Integer minPrice, Integer maxPrice) {

        // 1) 엔티티 조회
        final var book = QBookEntity.bookEntity;
        final var category = QBookCategoryEntity.bookCategoryEntity;
        final var subcategory = QBookSubcategoryEntity.bookSubcategoryEntity;

        // 2) 테이블 조인 조건(on)
        BooleanExpression onCategoryJoin = book.bookCategoryId.eq(category.id);
        BooleanExpression onSubcategoryJoin = book.bookSubcategoryId.eq(subcategory.id);

        // 3) 필터 조건(where)
        BooleanExpression predicate = book.status.eq(BookEntity.Status.VISIBLE)
                .and(book.deletedAt.isNull()); // 기본 조건 (status: VISIBLE, deletedAt: null 인 것)

        if (categoryCodeName != null && !"all".equals(categoryCodeName)) {
            predicate = predicate.and(
                    book.bookCategoryId.eq(category.id)
                            .and(category.categoryCodeName.eq(categoryCodeName))
            );
        } // null이 아니지만 categoryCodeName이 all 인 경우 미실행

        if (subcategoryCodeName != null && !"all".equals(subcategoryCodeName)) {
            predicate = predicate.and(
                    book.bookSubcategoryId.eq(subcategory.id)
                            .and(subcategory.subcategoryCodeName.eq(subcategoryCodeName))
            );
        } // null이 아니지만 subcategoryCodeName이 all 인 경우 미실행

        if (searchBy != null && searchKeyword != null && !searchKeyword.isBlank()) {
            predicate = predicate.and(buildSearchPredicate(searchBy, searchKeyword));
        }

        if (minPrice != null) {
            predicate = predicate.and(book.discountedPurchaseAmount.goe(minPrice));
        }

        if (maxPrice != null) {
            predicate = predicate.and(book.discountedPurchaseAmount.loe(maxPrice));
        }

        // 4) 페이징된 컨텐츠 조회 -> 리스트화
        List<BookEntity> entities = queryFactory
                .select(book)
                .from(book)
                .innerJoin(category).on(onCategoryJoin)
                .innerJoin(subcategory).on(onSubcategoryJoin)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .fetch();

        // 5) 전체 건수(count) 조회
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

        // 6) 엔티티 → 도메인 매핑
        List<BookSummary> content = entities.stream()
                .map(BookEntityMapper::toSummaryDomain)
                .toList();

        // 7) Page 구현체 반환
        return new PageImpl<>(content, pageable, totalItems);
    }

    /**
     * Spring Data Pageable의 Sort 정보를 QueryDSL OrderSpecifier로 변환
     */
    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        QBookEntity book = QBookEntity.bookEntity;
        PathBuilder<BookEntity> builder = new PathBuilder<>(BookEntity.class, book.getMetadata());

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
    private String mapToEntityField(String sortBy) {
        return switch (sortBy) {
            case "views"     -> "viewCount";
            case "sales"     -> "salesCount";
            case "rentals"   -> "rentalCount";
            case "price"     -> "discountedPurchaseAmount";
            case "published" -> "publishedAt";
            default          -> null; // sortBy가 예상 밖 값이면 무시
        };
    }

    /**
     * searchBy (title, author, publisher) 필드에 대해 LIKE 검색 조건 생성
     */
    private BooleanExpression buildSearchPredicate(String searchBy, String searchKeyword) {
        QBookEntity book = QBookEntity.bookEntity;
        String pattern = "%" + searchKeyword + "%";

        return switch (searchBy) {
            case "title"     -> book.title.likeIgnoreCase(pattern);
            case "author"    -> book.author.likeIgnoreCase(pattern);
            case "publisher" -> book.publisher.likeIgnoreCase(pattern);
            default          -> null; // searchBy가 예상 밖 값이면 무시
        };
    }
}
