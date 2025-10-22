package com.knockbook.backend.repository;

import com.knockbook.backend.domain.BookCategory;
import com.knockbook.backend.domain.BookSubcategory;
import com.knockbook.backend.entity.QBookCategoryEntity;
import com.knockbook.backend.entity.QVwBookCategoriesWithSubsEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookCategoryRepositoryImpl implements BookCategoryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<BookCategory> findBy(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        final var qc = QBookCategoryEntity.bookCategoryEntity;

        final var entity = queryFactory
                .selectFrom(qc)
                .where(qc.id.eq(id))
                .fetchOne();

        if (entity == null) {
            return Optional.empty();
        }

        final var res = BookCategory.builder()
                .id(entity.getId())
                .codeName(BookCategory.Category.valueOf(entity.getCategoryCodeName()))
                .displayName(entity.getCategoryDisplayName())
                .build();

        return Optional.of(res);
    }

    @Override
    public List<BookCategory> findAllCategories() {
        final var qc = QBookCategoryEntity.bookCategoryEntity;

        final var entities = queryFactory
                .selectFrom(qc)
                .orderBy(qc.id.asc())
                .fetch();

        return entities.stream()
                .map(e -> BookCategory.builder()
                        .id(e.getId())
                        .codeName(BookCategory.Category.valueOf(e.getCategoryCodeName()))
                        .displayName(e.getCategoryDisplayName())
                        .build())
                .toList();
    }

    @Override
    public List<BookSubcategory> findSubcategoriesByCategoryCodeName(String categoryCodeName) {
        if (categoryCodeName == null) return List.of();

        final var vw = QVwBookCategoriesWithSubsEntity.vwBookCategoriesWithSubsEntity;

        final var subcategoryEntities = queryFactory
                .select(vw.subcategoryId, vw.subcategoryCodeName, vw.subcategoryDisplayName)
                .from(vw)
                .where(vw.categoryCodeName.eq(categoryCodeName))
                .orderBy(vw.subcategoryId.asc())
                .fetch();

        return subcategoryEntities.stream()
                .map(t -> BookSubcategory.builder()
                        .id(t.get(vw.subcategoryId))
                        .codeName(BookSubcategory.Subcategory.valueOf(t.get(vw.subcategoryCodeName)))
                        .displayName(t.get(vw.subcategoryDisplayName))
                        .build())
                .toList();
    }

    @Override
    public boolean existsByCategoryCodeName(String categoryCodeName) {
        final var qc = QBookCategoryEntity.bookCategoryEntity;
        return queryFactory.selectFrom(qc)
                .where(qc.categoryCodeName.eq(categoryCodeName))
                .fetchFirst() != null;
    }
}
