package com.knockbook.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LoungePostCreateRequest {

    @NotBlank
    private String title;

    private String subtitle;

    @NotBlank
    private String content;

    private String previewImageUrl;
}
