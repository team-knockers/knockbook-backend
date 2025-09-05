package com.knockbook.backend.controller;

import com.knockbook.backend.dto.RegisterEmailRequest;
import com.knockbook.backend.dto.RegisterEmailResponse;
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

    // submit email for registration
    // the server will send authentication to the corresponding email
    @PostMapping(path = "/register/email")
    public ResponseEntity<RegisterEmailResponse> RegisterEmail(@RequestBody RegisterEmailRequest req) {
        final var validPeriod = Duration.ofSeconds(60);
        final var verificationToken = generateValidationToken(req.getEmail(), validPeriod);
        return ResponseEntity.accepted()
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(validPeriod.toSeconds()))
                .body(RegisterEmailResponse.builder()
                        .verificationToken(verificationToken)
                        .build());
    }

    private String generateValidationToken(final String email, final Duration duration) {
        // TODO
        throw new UnsupportedOperationException("Not Implemented yet");
    }
}
