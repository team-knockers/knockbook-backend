package com.knockbook.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetLoungePostDetailsResponse {

    private String id;
    private String displayName;
    private String avatarUrl; // nullable
    private String bio; // nullable
    private String title;
    private String subtitle; // nullable
    private String content;
    private Integer likeCount;
    private LocalDate createdAt;
}
