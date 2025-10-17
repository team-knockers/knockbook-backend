package com.knockbook.backend.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GetLoungePostCommentResponse {
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private LocalDate createdAt;
    private String editStatus; // Display edit status(null or '수정됨')
}
