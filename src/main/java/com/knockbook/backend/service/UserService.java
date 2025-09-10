package com.knockbook.backend.service;

import com.knockbook.backend.domain.User;
import com.knockbook.backend.repository.CredentialRepository;
import com.knockbook.backend.repository.IdentityRepository;
import com.knockbook.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IdentityRepository identityRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    @Transactional
    public User registerUser(final String email,
                             final String password,
                             final String displayName) {
        final var user = userRepository.insert(email, displayName);
        final var identity = identityRepository.insert(user.getId(), "local", email);
        final var hash = passwordEncoder.encode(password);
        credentialRepository.insert(identity.getId(), hash);
        return user;
    }

    public User getUser(final String email, final String password) {
        final var identity = identityRepository.findByProviderCodeAndSubject("local", email)
                .orElseThrow(() -> new BadCredentialsException("Identity not found"));

        final var credential = credentialRepository.findByIdentityId(identity.getId())
                .orElseThrow(() -> new BadCredentialsException("Credential not found"));

        if (!passwordEncoder.matches(password, credential.getPasswordHash())) {
            throw new BadCredentialsException("Invalid password");
        }

        return userRepository.findById(identity.getUserId())
                .orElseThrow(() -> new BadCredentialsException("User not found"));
    }
}
