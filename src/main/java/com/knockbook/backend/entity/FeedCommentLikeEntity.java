package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feeds_comment_likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FeedCommentLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_like_id", nullable = false)
    private Long commentLikeId;

    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}