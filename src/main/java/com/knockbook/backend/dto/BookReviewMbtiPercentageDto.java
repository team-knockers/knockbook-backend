package com.knockbook.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookReviewMbtiPercentageDto {

    private String mbti; // ISTJ, ISFJ, ...
    private Double percentage; // float with 0.1 precision
}
