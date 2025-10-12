package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "books_reviews_likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA default constructor
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder only
@Builder
public class BookReviewLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_review_like_id", nullable = false)
    private Long id;

    @Column(name = "book_review_id", nullable = false)
    private Long bookReviewId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "is_liked", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isLiked;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;
}
