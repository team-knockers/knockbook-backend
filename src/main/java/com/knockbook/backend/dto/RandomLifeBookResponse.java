package com.knockbook.backend.dto;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RandomLifeBookResponse {
    private String id;
    private String userId;
    private String displayName;
    private String bookId;
    private String coverThumbnailUrl;
    private String content;
}
