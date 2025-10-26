package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User {

    public enum Status { ACTIVE, PENDING, LOCKED }
    public enum Role { USER, ADMIN, MODERATOR }

    private Long id;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String mbti;
    private String bio;
    private Role role;
    private List<String> favoriteBookCategories;
    private Status status;
}
