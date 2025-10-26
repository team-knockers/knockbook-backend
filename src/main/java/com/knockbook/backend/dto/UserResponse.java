package com.knockbook.backend.dto;

import com.knockbook.backend.domain.User;
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
    @NotBlank
    private String role;
    private String bio;
    private List<String> favoriteBookCategories;

    public static UserResponse fromDomain(User domain) {
        return UserResponse.builder()
                .id(domain.getId().toString())
                .email(domain.getEmail())
                .displayName(domain.getDisplayName())
                .avatarUrl(domain.getAvatarUrl())
                .mbti(domain.getMbti())
                .role(domain.getRole().name())
                .bio(domain.getBio())
                .favoriteBookCategories(domain.getFavoriteBookCategories())
                .build();
    }
}
