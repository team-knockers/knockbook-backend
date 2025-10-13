package com.knockbook.backend.domain;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BookReviewStatistic {

    private Double averageRating;
    private Long totalCount;
    private List<ScoreCount> scoreCounts;
    private List<MbtiCount> mbtiCounts;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScoreCount {
        private Integer score;
        private Long count;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MbtiCount {
        private String mbti;
        private Long count;
    }
}
