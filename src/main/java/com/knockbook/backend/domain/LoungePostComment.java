package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class LoungePostComment {

    public enum Status { VISIBLE, HIDDEN }

    private final Long id;
    private final Long postId;
    private final Long userId;
    private final String displayName;
    private final String avatarUrl;
    private final String content;
    private final Status status;
    private final Instant createdAt;
    private final Instant updatedAt;
}
