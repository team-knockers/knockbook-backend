package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProductInquiry {
    private Long inquiryId;
    private String displayName;
    private String title;
    private String questionBody;
    private Instant createdAt;

    private String answerBody;  // nullable
    private Instant answeredAt; // nullable
}
