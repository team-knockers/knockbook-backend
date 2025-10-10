package com.knockbook.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookReviewDto {

    private String id;
    private String bookId;
    private String userId;
    private String displayName;
    private String mbti;
    private String transactionType;
    private Instant createdAt;
    private String content;
    private Integer rating;
    private List<String> imageUrls;
    private Integer likesCount;
    private Boolean likedByMe;
}
