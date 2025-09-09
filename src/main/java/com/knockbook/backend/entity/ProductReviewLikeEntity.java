package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products_review_likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProductReviewLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_likes_id", nullable = false)
    private Long reviewLikesId;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "is_liked", nullable = false)
    @Builder.Default
    private Boolean isLiked = true;
}