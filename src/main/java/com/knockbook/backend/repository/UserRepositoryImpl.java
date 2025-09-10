package com.knockbook.backend.repository;

import com.knockbook.backend.domain.User;
import com.knockbook.backend.entity.QBookCategoryEntity;
import com.knockbook.backend.entity.QUserEntity;
import com.knockbook.backend.entity.QUserFavoriteBookCategoryEntity;
import com.knockbook.backend.entity.UserEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

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
        final var qFavoriteBookCateories = QUserFavoriteBookCategoryEntity.userFavoriteBookCategoryEntity;
        final var qBookCategory = QBookCategoryEntity.bookCategoryEntity;

        final var userEntity = queryFactory
                .selectFrom(qUser)
                .where(qUser.id.eq(id))
                .fetchOne();

        if (userEntity == null) {
            return Optional.empty();
        }

        final var favoriteBookCategoryCodes = queryFactory
                .select(qBookCategory.categoryCodeName)
                .from(qFavoriteBookCateories)
                .join(qBookCategory)
                .on(qBookCategory.id.eq(qFavoriteBookCateories.bookCategoryId))
                .where(qFavoriteBookCateories.userId.eq(id))
                .distinct()
                .fetch();

        final var user = User.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .displayName(userEntity.getDisplayName())
                .avatarUrl(userEntity.getAvatarUrl())
                .mbti(userEntity.getMbti())
                .favoriteBookCategories(favoriteBookCategoryCodes)
                .status(User.Status.valueOf(userEntity.getStatus().name()))
                .build();

        return Optional.of(user);
    }
}
