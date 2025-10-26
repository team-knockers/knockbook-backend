package com.knockbook.backend.controller;

import com.knockbook.backend.service.KakaoPayService;
import com.knockbook.backend.service.TokenService;
import com.knockbook.backend.service.UserService;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping(path = "/checkout/kakao")
@RequiredArgsConstructor
public class KakaoPayPublicController {

    private final KakaoPayService kakaoPayService;
    private final TokenService tokenService;
    private final UserService userService;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    // Kakao → server(success)
    @GetMapping("/success")
    public ResponseEntity<Void> success(
            @RequestParam Long orderId,
            @RequestParam("pg_token") String pgToken) throws JOSEException {

        final var result = kakaoPayService.approve(orderId, pgToken);
        final var userId = result.getUserId();
        final var role = userService.getUser(userId).getRole().name();
        final var subject = String.valueOf(userId);
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

        final var next = "/order/" + orderId + "/complete";
        final var redirect = frontendBaseUrl + "/auth/callback?next=" + URLEncoder.encode(next, StandardCharsets.UTF_8);

        return ResponseEntity.status(302)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .location(URI.create(redirect))
                .build();
    }

    @GetMapping("/cancel")
    public ResponseEntity<Void> cancel(@RequestParam Long orderId) {
        return ResponseEntity.status(302)
                .location(URI.create(frontendBaseUrl + "/order/" + orderId + "/cancelled"))
                .build();
    }

    @GetMapping("/fail")
    public ResponseEntity<Void> fail(@RequestParam Long orderId) {
        return ResponseEntity.status(302)
                .location(URI.create(frontendBaseUrl + "/order/" + orderId + "/failed"))
                .build();
    }
}
