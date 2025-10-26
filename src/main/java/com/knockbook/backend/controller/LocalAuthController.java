package com.knockbook.backend.controller;

import com.knockbook.backend.dto.*;
import com.knockbook.backend.service.LocalAuthService;
import com.knockbook.backend.service.TokenService;
import com.knockbook.backend.service.UserService;
import com.nimbusds.jose.JOSEException;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.time.Duration;

@RestController
@RequestMapping(path = "/auth/local")
public class LocalAuthController {

    @Autowired
    private LocalAuthService localAuthService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserService userService;

    // submit email for registration
    // the server will send authentication to the corresponding email
    @PostMapping(path = "/register/email")
    public ResponseEntity<EmailVerificationTokenResponse> registerEmail(
            @Valid @RequestBody RegisterEmailRequest req)
            throws JOSEException, MessagingException {
        final var email = req.getEmail();
        final var validPeriod = Duration.ofMinutes(10);
        final var emailVerificationToken =
                localAuthService.sendCodeViaEmailAndIssueVerificationToken(email, validPeriod);
        return ResponseEntity.accepted()
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(validPeriod.toSeconds()))
                .body(EmailVerificationTokenResponse.builder()
                        .emailVerificationToken(emailVerificationToken)
                        .build());
    }

    @PostMapping(path = "/register/email/verify")
    public ResponseEntity<EmailRegistrationTokenResponse> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest req)
            throws ParseException, JOSEException {
        final var emailVerificationToken = req.getEmailVerificationToken();
        final var code = req.getCode();
        final var validPeriod = Duration.ofMinutes(30);
        final var emailRegistrationToken =
                localAuthService.verifyEmailVerificationTokenAndIssueRegistrationToken(emailVerificationToken, code, validPeriod);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EmailRegistrationTokenResponse.builder()
                        .registrationToken(emailRegistrationToken)
                        .build());
    }

    @PostMapping(path = "/register/complete")
    public ResponseEntity<LocalLoginResponse> completeRegistration(
            @Valid @RequestBody CompleteRegisterRequest req)
            throws ParseException, JOSEException {
        final var registrationToken = req.getRegistrationToken();
        final var subject = localAuthService.getSubjectFromRegistrationToken(registrationToken);
        final var user = userService.registerUser(subject, req.getPassword(), req.getDisplayName());
        final var role = user.getRole().name();

        // issues refresh and access token
        final var tokens = tokenService.issueTokens(subject, role);

        // set refresh token as HttpOnly cookie
        final var cookieName = TokenService.refreshTokenCookieName;
        final var refreshCookie = ResponseCookie.from(cookieName, tokens.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/auth/token")
                .sameSite("None")
                .maxAge(TokenService.refreshTokenValidPeriod)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(LocalLoginResponse.builder()
                        .accessToken(tokens.getAccessToken())
                        .userId(user.getId().toString())
                        .build());
    }

    @PostMapping(path = "/login")
    public ResponseEntity<LocalLoginResponse> login(
            @Valid @RequestBody LocalLoginRequest req)
            throws JOSEException {
        final var user = userService.getUser(req.getEmail(), req.getPassword());
        final var subject = user.getId().toString();
        final var role = user.getRole().name();
        final var tokens = tokenService.issueTokens(subject, role);

        // set refresh token as HttpOnly cookie
        final var cookieName = TokenService.refreshTokenCookieName;
        final var refreshCookie = ResponseCookie.from(cookieName, tokens.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/auth/token")
                .sameSite("None")
                .maxAge(TokenService.refreshTokenValidPeriod)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(LocalLoginResponse.builder()
                        .accessToken(tokens.getAccessToken())
                        .userId(subject)
                        .build());
    }
}
