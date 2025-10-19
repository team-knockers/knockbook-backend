package com.knockbook.backend.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LoungePostCommentDto {
    private String id;
    private String postId;
    private String userId;
    private String displayName;
    private String avatarUrl;
    private String content;
    private LocalDate createdAt;
    private String editStatus; // null 또는 "수정됨"
}
