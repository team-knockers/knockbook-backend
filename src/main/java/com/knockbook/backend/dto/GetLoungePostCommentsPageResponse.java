package com.knockbook.backend.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GetLoungePostCommentsPageResponse {
    private List<LoungePostCommentDto> comments;
    private int page;
    private int size;
    private int totalItems;
    private int totalPages;
}
