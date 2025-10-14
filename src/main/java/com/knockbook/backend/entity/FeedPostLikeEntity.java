package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feeds_post_likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FeedPostLikeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_like_id", nullable = false)
    private Long postLikeId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}
