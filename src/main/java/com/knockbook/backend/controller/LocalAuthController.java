package com.knockbook.backend.controller;

import com.knockbook.backend.dto.EmailVerificationTokenResponse;
import com.knockbook.backend.dto.RegisterEmailRequest;
import com.knockbook.backend.service.EmailVerificationService;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Duration;

@Controller
@RequestMapping(path = "/auth/local")
public class LocalAuthController {

    @Autowired
    private EmailVerificationService emailVerificationService;

    // submit email for registration
    // the server will send authentication to the corresponding email
    @PostMapping(path = "/register/email")
    public ResponseEntity<EmailVerificationTokenResponse> RegisterEmail(@RequestBody RegisterEmailRequest req) throws JOSEException {
        final var validPeriod = Duration.ofSeconds(600);
        final var emailVerificationToken = emailVerificationService.issueAndSend(req.getEmail(), validPeriod);
        return ResponseEntity.accepted()
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(validPeriod.toSeconds()))
                .body(EmailVerificationTokenResponse.builder()
                        .emailVerificationToken(emailVerificationToken)
                        .build());
    }
}
