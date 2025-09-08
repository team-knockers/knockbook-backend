package com.knockbook.backend.service;

import com.knockbook.backend.component.JWTComponent;
import com.knockbook.backend.domain.User;
import com.knockbook.backend.repository.CredentialRepository;
import com.knockbook.backend.repository.IdentityRepository;
import com.knockbook.backend.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
public class LocalRegistrationService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IdentityRepository identityRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private JWTComponent jwtComponent;

    @Transactional
    public User completeRegistration(final String registrationToken,
                                     final String password,
                                     final String displayName) throws ParseException, JOSEException {
        // Parse & validate registration token (JWS)
        final var claims = jwtComponent.parseJWS(registrationToken,
                JWTComponent.Audience.EMAIL_REGISTRATION_HANDLER);

        // Create user
        final var email = claims.getSubject();
        final var user = userRepository.insert(email, displayName);

        // Create identity (local)
        final var identity = identityRepository.insert(user.getId(), "local", email);

        // Hash password & create credential
        final var hash = passwordEncoder.encode(password);
        credentialRepository.insert(identity.getId(), hash);

        // returns user
        return user;
    }
}
