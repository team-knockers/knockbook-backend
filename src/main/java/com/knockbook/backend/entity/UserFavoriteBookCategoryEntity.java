package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "user_favorite_book_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA default constructor
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder only
@Builder
@IdClass(UserFavoriteBookCategoryEntity.Pk.class)
public class UserFavoriteBookCategoryEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "book_category_id")
    private Long bookCategoryId;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Pk implements Serializable {
        private Long userId;
        private Long bookCategoryId;
    }
}
