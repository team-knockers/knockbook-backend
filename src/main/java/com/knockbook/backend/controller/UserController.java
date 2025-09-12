package com.knockbook.backend.controller;

import com.knockbook.backend.domain.User;
import com.knockbook.backend.dto.UserResponse;
import com.knockbook.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PreAuthorize("#userId == authentication.name")
    @PutMapping("/{userId}")
    public ResponseEntity<Void> updateUser(
            @PathVariable("userId") String userId,
            @RequestParam(required = false) String displayName,
            @RequestParam(required = false) String avatarUrl,
            @RequestParam(required = false) String mbti,
            @RequestParam(required = false) List<String> favoriteBookCategories) {
        final var patch = User.builder()
                .id(Long.valueOf(userId))
                .displayName(displayName)
                .avatarUrl(avatarUrl)
                .mbti(mbti)
                .favoriteBookCategories(favoriteBookCategories)
                .build();
        userService.updateUserProfile(patch);
        return ResponseEntity.noContent().build(); // 204
    }
}
