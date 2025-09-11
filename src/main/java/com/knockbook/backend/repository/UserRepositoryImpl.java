package com.knockbook.backend.repository;

import com.knockbook.backend.domain.User;
import com.knockbook.backend.entity.*;
import com.knockbook.backend.exception.UserNotFoundException;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final EntityManager em;
    private final JPAQueryFactory qf;

    @Override
    public User insert(String email, String displayName) {
        final var userEntity = UserEntity.builder()
                .email(email)
                .displayName(displayName)
                .status(UserEntity.Status.ACTIVE)
                .build();
        em.persist(userEntity);  // insert (Id generates automatically)
        em.flush(); // get PK
        return User.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .displayName(userEntity.getDisplayName())
                .build();
    }

    @Override
    public Optional<User> findById(Long id) {
        final var qUser = QUserEntity.userEntity;
        final var qFavoriteCode = QUserFavoriteBookCategoryEntity.userFavoriteBookCategoryEntity;
        final var qCategory = QBookCategoryEntity.bookCategoryEntity;

        final var userEntity = qf.selectFrom(qUser).from(qUser).where(qUser.id.eq(id)).fetchOne();

        if (userEntity == null) { return Optional.empty(); }

        final var favoriteCodes = qf
                .select(qCategory.categoryCodeName)
                .from(qFavoriteCode)
                .join(qCategory).on(qCategory.id.eq(qFavoriteCode.bookCategoryId))
                .where(qFavoriteCode.userId.eq(id))
                .fetch();

        final var user = User.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .displayName(userEntity.getDisplayName())
                .avatarUrl(userEntity.getAvatarUrl())
                .mbti(userEntity.getMbti())
                .favoriteBookCategories(favoriteCodes)
                .status(User.Status.valueOf(userEntity.getStatus().name()))
                .build();

        return Optional.of(user);
    }

    @Override
    @Transactional
    public void update(User patch) {
        final var userId = patch.getId();

        // Update scalar fields (use affected rows fo detect existence)
        final var qUser = QUserEntity.userEntity;
        final var updateClause = qf.update(qUser).where(qUser.id.eq(userId));
        var scalarChanged = false;

        if (patch.getDisplayName() != null) {
            updateClause.set(qUser.displayName, patch.getDisplayName());
            scalarChanged = true;
        }
        if (patch.getAvatarUrl() != null) {
            updateClause.set(qUser.avatarUrl, patch.getAvatarUrl());
            scalarChanged = true;
        }
        if (patch.getMbti() != null) {
            updateClause.set(qUser.mbti, patch.getMbti());
            scalarChanged = true;
        }

        final var affected = scalarChanged ? updateClause.execute() : 0L;

        if (scalarChanged && affected == 0) {
            throw new UserNotFoundException(userId);
        }

        // Apply delta update for favorite book categories
        final var favorites = patch.getFavoriteBookCategories();
        if (favorites != null) {
            final var qFavorite = QUserFavoriteBookCategoryEntity.userFavoriteBookCategoryEntity;
            final var qCategory = QBookCategoryEntity.bookCategoryEntity;

            if (favorites.isEmpty()) {
                // empty list -> delete all favorites
                qf.delete(qFavorite).where(qFavorite.userId.eq(userId)).execute();
            }
            else {
                // delta update (delete only removed ones, insert new ones)
                final var distinctCodes = favorites.stream().distinct().toList();

                // Map codes -> category IDs (validate existence)
                final var tuples = qf
                        .select(qCategory.categoryCodeName, qCategory.id)
                        .from(qCategory)
                        .where(qCategory.categoryCodeName.in(distinctCodes))
                        .fetch();

                final var codeToId = new HashMap<String, Long>(tuples.size());
                for (final var t : tuples) {
                    codeToId.put(t.get(qCategory.categoryCodeName), t.get(qCategory.id));
                }

                if (codeToId.size() != distinctCodes.size()) {
                    throw new IllegalArgumentException("Invalid category code included");
                }

                // Load current favorite book category IDs
                final var currentIds = new java.util.HashSet<Long>(
                        qf.select(qFavorite.bookCategoryId)
                                .from(qFavorite)
                                .where(qFavorite.userId.eq(userId))
                                .fetch()
                );

                // Calculate desired IDs
                final var desiredIds = new java.util.HashSet<Long>(codeToId.values());

                // Delete only categories not in desired IDs
                if (!currentIds.isEmpty()) {
                    qf.delete(qFavorite)
                            .where(qFavorite.userId.eq(userId).and(qFavorite.bookCategoryId.notIn(desiredIds)))
                            .execute();
                }

                // Insert only new categories (desired - current)
                desiredIds.removeAll(currentIds);
                for (final var categoryId : desiredIds) {
                    em.persist(UserFavoriteBookCategoryEntity.builder()
                            .userId(userId)
                            .bookCategoryId(categoryId)
                            .build()
                    );
                }
            }
        }

        em.flush();
    }
}
