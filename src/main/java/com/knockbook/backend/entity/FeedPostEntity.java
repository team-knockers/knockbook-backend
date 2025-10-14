package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "feeds_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FeedPostEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder.Default
    @Column(name = "content", length = 1000, nullable = false)
    private String content = "";

    @Builder.Default
    @Column(name = "likes_count", nullable = false)
    private Integer likesCount = 0;

    @Builder.Default
    @Column(name = "comments_count", nullable = false)
    private Integer commentsCount = 0;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
