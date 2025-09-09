package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "books_wishlists")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA default constructor
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder only
@Builder
public class BookWishlistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_wishlist_id", nullable = false)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "is_wished", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isWished;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at", insertable = false, updatable = true)
    private Instant deletedAt;
}
