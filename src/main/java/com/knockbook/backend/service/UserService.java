package com.knockbook.backend.service;

import com.knockbook.backend.component.ImgbbUploader;
import com.knockbook.backend.domain.User;
import com.knockbook.backend.exception.CredentialNotFoundException;
import com.knockbook.backend.exception.IdentityNotFoundException;
import com.knockbook.backend.exception.UserNotFoundException;
import com.knockbook.backend.repository.CredentialRepository;
import com.knockbook.backend.repository.IdentityRepository;
import com.knockbook.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    final private PasswordEncoder passwordEncoder;
    final private UserRepository userRepository;
    final private IdentityRepository identityRepository;
    final private CredentialRepository credentialRepository;
    final private CouponService couponService;
    final private ImgbbUploader imgbbUploader;

    @Transactional
    public User registerUser(final String email,
                             final String password,
                             final String displayName) {
        final var user = userRepository.insert(email, displayName);
        final var identity = identityRepository.insert(user.getId(), "local", email);
        final var hash = passwordEncoder.encode(password);
        credentialRepository.insert(identity.getId(), hash);
        try {
            couponService.grantWelcomeCoupons(user.getId());
        } catch (Exception ignored) {}
        return user;
    }

    public User getUser(final String email,
                        final String password) {
        final var identity = identityRepository.findByProviderCodeAndSubject("local", email)
                .orElseThrow(() -> new IdentityNotFoundException(email));

        final var identityId = identity.getId();
        final var credential = credentialRepository.findByIdentityId(identityId)
                .orElseThrow(() -> new CredentialNotFoundException(identityId));

        if (!passwordEncoder.matches(password, credential.getPasswordHash())) {
            throw new BadCredentialsException("Invalid password");
        }

        final var userId = identity.getUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public User getUser(final Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public void verifyPassword(final Long userId, final String password) {
        final var identity = identityRepository.findByUserId(userId)
                .orElseThrow(() -> new IdentityNotFoundException(userId));
        final var identityId = identity.getId();
        final var credential = credentialRepository.findByIdentityId(identityId)
                .orElseThrow(() -> new CredentialNotFoundException(identityId));

        if (!passwordEncoder.matches(password, credential.getPasswordHash())) {
            throw new BadCredentialsException("Invalid password");
        }
    }

    public void changePassword(final Long userId, final String password) {
        final var identity = identityRepository.findByUserId(userId)
                .orElseThrow(() -> new IdentityNotFoundException(userId));
        final var hash = passwordEncoder.encode(password);
        credentialRepository.update(identity.getId(), hash);
    }

    public void updateProfile(final User patch) {
        userRepository.update(patch);
    }

    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("EMPTY_FILE");
        }
        final String url = imgbbUploader.upload(file);
        userRepository.update(User.builder()
                .id(userId).avatarUrl(url).build());
        return url;
    }
}
