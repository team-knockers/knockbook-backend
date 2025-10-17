package com.knockbook.backend.dto;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LoungePostCommentDTO {
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private String createdAt;
    private String updatedAt; // 수정여부를 표기
}
