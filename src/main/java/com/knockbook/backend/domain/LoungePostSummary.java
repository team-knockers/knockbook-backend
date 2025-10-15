package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class LoungePostSummary {

    public enum Status { VISIBLE, HIDDEN }

    private final Long id;
    private final Long userId;
    private final String displayName;
    private final String title;
    private final String previewImageUrl; // nullable
    private final Status status;
    private final Integer likeCount;
    private final Instant createdAt;
}
