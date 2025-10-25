package com.knockbook.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserResponse {

    @NotBlank
    private String id;
    @NotBlank
    private String email;
    @NotBlank
    private String displayName;
    private String avatarUrl;
    private String mbti;
    private String bio;
    private List<String> favoriteBookCategories;
}
