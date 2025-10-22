package com.knockbook.backend.dto;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BookReviewImageDto {

    private String imageUrl;
    private Integer sortOrder;
}
