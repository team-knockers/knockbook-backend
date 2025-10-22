package com.knockbook.backend.domain;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class BookReview {

    public enum TransactionType { PURCHASE, RENTAL }

    private Long id;
    private Long bookId;
    private Long userId;
    private String displayName;
    private String mbti;
    private TransactionType transactionType;
    private String content;
    private Integer rating;
    private List<BookReviewImage> imageUrls;
    private Integer likesCount;
    private Instant createdAt;
}
