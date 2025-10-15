package com.knockbook.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoungePostSummaryDto {

    private String id;
    private String displayName;
    private String title;
    private String previewImageUrl;
    private Integer likeCount;
    private Instant createdAt;
}
