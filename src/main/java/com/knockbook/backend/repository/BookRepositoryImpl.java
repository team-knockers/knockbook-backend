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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public Map<Long, BookEntity> findByIdsAsMap(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        final var cb = em.getCriteriaBuilder();
        final var cq = cb.createQuery(BookEntity.class);
        final var root = cq.from(BookEntity.class);
        cq.select(root).where(root.get("id").in(ids));
        final var list = em.createQuery(cq).getResultList();
        final var map = new HashMap<Long, BookEntity>(list.size() * 2);
        for (var b : list) {
            map.put(b.getId(), b);
        }
        return map;
    }

    @Override
    public Page<BookSummary> findBooksByCondition(
            String categoryCodeName, String subcategoryCodeName, Pageable pageable, String searchBy,
            String searchKeyword, Integer minPrice, Integer maxPrice) {

        // 1) Define query entities
        final var book = QBookEntity.bookEntity;
        final var category = QBookCategoryEntity.bookCategoryEntity;
        final var subcategory = QBookSubcategoryEntity.bookSubcategoryEntity;

        // 2) Define join conditions
        BooleanExpression onCategoryJoin = book.bookCategoryId.eq(category.id);
        BooleanExpression onSubcategoryJoin = book.bookSubcategoryId.eq(subcategory.id);

        // 3) Build filtering conditions (WHERE clause)
        BooleanExpression predicate = book.status.eq(BookEntity.Status.VISIBLE)
                .and(book.deletedAt.isNull()); // Basic condition: only visible and not deleted books

        if (categoryCodeName != null && !"all".equals(categoryCodeName)) {
            predicate = predicate.and(
                    book.bookCategoryId.eq(category.id)
                            .and(category.categoryCodeName.eq(categoryCodeName))
            );
        } // Skip if categoryCodeName is "all"

        if (subcategoryCodeName != null && !"all".equals(subcategoryCodeName)) {
            predicate = predicate.and(
                    book.bookSubcategoryId.eq(subcategory.id)
                            .and(subcategory.subcategoryCodeName.eq(subcategoryCodeName))
            );
        } // Skip if subcategoryCodeName is "all"

        if (searchBy != null && searchKeyword != null && !searchKeyword.isBlank()) {
            predicate = predicate.and(buildSearchPredicate(searchBy, searchKeyword));
        }

        if (minPrice != null) {
            predicate = predicate.and(book.discountedPurchaseAmount.goe(minPrice));
        }

        if (maxPrice != null) {
            predicate = predicate.and(book.discountedPurchaseAmount.loe(maxPrice));
        }

        // 4) Execute paged query and fetch results
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

        // 5) Execute count query
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

        // 6) Map entity to domain object
        List<BookSummary> content = entities.stream()
                .map(BookEntityMapper::toSummaryDomain)
                .toList();

        // 7) Return paginated result
        return new PageImpl<>(content, pageable, totalItems);
    }

    /**
     * Converts Spring Data Sort to QueryDSL OrderSpecifiers
     */
    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        QBookEntity book = QBookEntity.bookEntity;
        PathBuilder<BookEntity> builder = new PathBuilder<>(BookEntity.class, book.getMetadata());

        return sort.stream()
                .map(order -> {
                    String prop = mapToEntityField(order.getProperty());
                    Order direction = order.isAscending() ? Order.ASC : Order.DESC;

                    // Handle date-only field (publishedAt)
                    if ("publishedAt".equals(prop)) {
                        DatePath<LocalDate> datePath =
                                builder.getDate(prop, LocalDate.class);
                        return new OrderSpecifier<>(direction, datePath);
                    }

                    // Handle numeric fields (e.g., viewCount, salesCount, etc.)
                    NumberPath<Integer> numPath =
                            builder.getNumber(prop, Integer.class);
                    return new OrderSpecifier<>(direction, numPath);
                })
                .toArray(OrderSpecifier[]::new);
    }

    /**
     * Maps sortBy string to entity field name
     */
    private String mapToEntityField(String sortBy) {
        return switch (sortBy) {
            case "views"     -> "viewCount";
            case "sales"     -> "salesCount";
            case "rentals"   -> "rentalCount";
            case "price"     -> "discountedPurchaseAmount";
            case "published" -> "publishedAt";
            default          -> throw new IllegalArgumentException("Invalid sortBy value: " + sortBy);
        };
    }

    /**
     * Builds a LIKE predicate for the given searchBy and keyword
     */
    private BooleanExpression buildSearchPredicate(String searchBy, String searchKeyword) {
        QBookEntity book = QBookEntity.bookEntity;
        String pattern = "%" + searchKeyword + "%";

        return switch (searchBy) {
            case "title"     -> book.title.likeIgnoreCase(pattern);
            case "author"    -> book.author.likeIgnoreCase(pattern);
            case "publisher" -> book.publisher.likeIgnoreCase(pattern);
            default          -> throw new IllegalArgumentException("Invalid searchBy value: " + searchBy);
        };
    }
}
