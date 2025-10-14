package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feeds_post_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FeedPostImageEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", nullable = false)
    private Long imageId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "image_url", length = 1024, nullable = false)
    private String imageUrl;
}
