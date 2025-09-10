package com.knockbook.backend.controller;

import com.knockbook.backend.dto.UserResponse;
import com.knockbook.backend.service.UserService;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("#userId == authentication.name")
    @GetMapping(path = "/{userId}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable("userId") String userId) {
        final var user = userService.getUser(Long.valueOf(userId));
        return ResponseEntity.ok()
                .body(UserResponse.builder()
                        .id(user.getId().toString())
                        .email(user.getEmail())
                        .displayName(user.getDisplayName())
                        .avartarUrl(user.getAvatarUrl())
                        .mbti(user.getMbti())
                        .favoriteBookCategories(user.getFavoriteBookCategories())
                        .build());
    }
}
