package com.knockbook.backend.controller;

import com.knockbook.backend.dto.*;
import com.knockbook.backend.service.EmailVerificationService;
import com.knockbook.backend.service.LocalRegistrationService;
import com.knockbook.backend.service.TokenService;
import com.nimbusds.jose.JOSEException;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.ParseException;
import java.time.Duration;

@Controller
@RequestMapping(path = "/auth/local")
public class LocalAuthController {

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private LocalRegistrationService localRegistrationService;

    @Autowired
    private TokenService tokenService;

    // submit email for registration
    // the server will send authentication to the corresponding email
    @PostMapping(path = "/register/email")
    public ResponseEntity<EmailVerificationTokenResponse> registerEmail(
            @RequestBody RegisterEmailRequest req)
            throws JOSEException, MessagingException {
        final var email = req.getEmail();
        final var validPeriod = Duration.ofMinutes(10);
        final var emailVerificationToken =
                emailVerificationService.sendCodeAndIssueVerificationToken(email, validPeriod);
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
                emailVerificationService.verifyAndIssueRegistrationToken(emailVerificationToken, code, validPeriod);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EmailRegistrationTokenResponse.builder()
                        .emailRegistrationToken(emailRegistrationToken)
                        .build());
    }

    @PostMapping(path = "/register/complete")
    public ResponseEntity<AccessTokenResponse> completeRegistration(
            @Valid @RequestBody CompleteRegisterRequest req)
            throws ParseException, JOSEException {
        final var registrationToken = req.getRegistrationToken();
        final var password = req.getPassword();
        final var displayName = req.getDisplayName();
        final var user = localRegistrationService.completeRegistration(registrationToken, password, displayName);
        final var subject = user.getId().toString();
        final var tokens = tokenService.issueTokens(subject);

        // set refresh token as HttpOnly cookie
        final var cookieName = TokenService.refreshTokenCookieName;
        final var refreshCookie = ResponseCookie.from(cookieName, tokens.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/auth/token/refresh")
                .sameSite("None")
                .maxAge(TokenService.refreshTokenValidPeriod)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(AccessTokenResponse.builder()
                        .accessToken(tokens.getAccessToken())
                        .build());
    }
}
