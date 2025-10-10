package com.knockbook.backend.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProductInquiryDTO {
    private String inquiryId;
    private String displayName;
    private String title;
    private String questionBody;
    private String createdAt;

    private String answerBody;  // nullable
    private String answeredAt;  // nullable
    private String status;  // "ANSWERED" | "WAITING"
}
