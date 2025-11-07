package com.knockbook.backend.controller;

import com.knockbook.backend.domain.User;
import com.knockbook.backend.dto.ChangePasswordRequest;
import com.knockbook.backend.dto.UploadAvatarResponse;
import com.knockbook.backend.dto.VerifyPasswordRequest;
import com.knockbook.backend.dto.UserResponse;
import com.knockbook.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        final var dto = UserResponse.fromDomain(user);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("#userId == authentication.name")
    @PutMapping("/{userId}")
    public ResponseEntity<Void> updateProfile(
            @PathVariable("userId") String userId,
            @RequestParam(required = false) String displayName,
            @RequestParam(required = false) String avatarUrl,
            @RequestParam(required = false) String mbti,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) List<String> favoriteBookCategories) {

        final var patch = User.builder()
                .id(Long.valueOf(userId))
                .displayName(displayName)
                .avatarUrl(avatarUrl)
                .mbti(mbti)
                .bio(bio)
                .favoriteBookCategories(favoriteBookCategories)
                .build();
        userService.updateProfile(patch);
        return ResponseEntity.noContent().build(); // 204
    }

    @PreAuthorize("#userId == authentication.name")
    @PutMapping("/{userId}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable("userId") String userId,
            @RequestBody ChangePasswordRequest req
    ) {
        userService.changePassword(Long.valueOf(userId), req.getPassword());
        return ResponseEntity.noContent().build(); // 204
    }

    @PreAuthorize("#userId == authentication.name")
    @PostMapping("/{userId}/password/verify")
    public ResponseEntity<Void> confirmPassword(
            @PathVariable("userId") String userId,
            @RequestBody VerifyPasswordRequest req
    ) {
        userService.verifyPassword(Long.valueOf(userId), req.getPassword());
        return ResponseEntity.noContent().build(); // 204
    }

    @PreAuthorize("#userId == authentication.name")
    @PostMapping(path="/{userId}/avatar", consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadAvatarResponse> uploadAvatar(
            @PathVariable String userId,
            @RequestPart("file") MultipartFile file) {
        final var url = userService.uploadAvatar(Long.valueOf(userId), file);
        final var dto = UploadAvatarResponse.builder()
                .avatarUrl(url).build();
        return ResponseEntity.ok(dto);
    }
}
