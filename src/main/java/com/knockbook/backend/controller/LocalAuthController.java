package com.knockbook.backend.controller;

import com.knockbook.backend.dto.EmailRegistrationTokenResponse;
import com.knockbook.backend.dto.EmailVerificationTokenResponse;
import com.knockbook.backend.dto.RegisterEmailRequest;
import com.knockbook.backend.dto.VerifyEmailRequest;
import com.knockbook.backend.service.EmailVerificationService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

    // submit email for registration
    // the server will send authentication to the corresponding email
    @PostMapping(path = "/register/email")
    public ResponseEntity<EmailVerificationTokenResponse> registerEmail(
            @RequestBody RegisterEmailRequest req)
            throws JOSEException {
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
        final var validPeriod = Duration.ofMinutes(10);
        final var emailRegistrationToken =
                emailVerificationService.verifyAndIssueRegistrationToken(emailVerificationToken, code, validPeriod);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EmailRegistrationTokenResponse.builder()
                        .emailRegistrationToken(emailRegistrationToken)
                        .build());
    }
}
