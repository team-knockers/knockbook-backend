package com.knockbook.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetBookReviewStatisticsResponse {

    private Double averageRating;
    private Integer reviewCount;
    private List<BookReviewScoreCountDto> scoreCounts;
    private List<BookReviewMbtiPercentageDto> mbtiPercentage;
}
