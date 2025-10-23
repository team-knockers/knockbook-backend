package com.knockbook.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.knockbook.backend.domain.BookReview;
import com.knockbook.backend.domain.BookReviewImage;
import lombok.*;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BookReviewSummaryDTO {

    private String id;
    private String bookId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "Asia/Seoul")
    private Instant createdAt;

    private String content;
    private Integer rating;
    private List<String> imageUrls;

    public static BookReviewSummaryDTO from(BookReview review) {
        return BookReviewSummaryDTO.builder()
                .id(String.valueOf(review.getId()))
                .bookId(String.valueOf(review.getBookId()))
                .createdAt(review.getCreatedAt()
                        .atZone(ZoneId.of("Asia/Seoul"))
                        .toInstant())
                .content(review.getContent())
                .rating(review.getRating())
                .imageUrls(review.getImageUrls() == null ? List.of()
                        : review.getImageUrls().stream()
                        .map(BookReviewImage::getImageUrl)
                        .toList())
                .build();
    }
}
