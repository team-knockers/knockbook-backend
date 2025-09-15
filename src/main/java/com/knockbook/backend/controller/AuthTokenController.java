package com.knockbook.backend.controller;

import com.knockbook.backend.dto.LocalLoginResponse;
import com.knockbook.backend.service.TokenService;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping(path = "/auth/token")
public class AuthTokenController {

    @Autowired
    private TokenService tokenService;

    @PostMapping("/refresh")
    public ResponseEntity<LocalLoginResponse> refresh(
            @CookieValue(TokenService.refreshTokenCookieName) String refreshToken)
            throws ParseException, JOSEException { // required=true

        final var tokens = tokenService.refreshTokens(refreshToken);
        final var cookie = ResponseCookie.from(TokenService.refreshTokenCookieName, tokens.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/auth/token")
                .maxAge(TokenService.refreshTokenValidPeriod)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(LocalLoginResponse.builder()
                        .accessToken(tokens.getAccessToken())
                        .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(TokenService.refreshTokenCookieName) String refreshToken) {
        final var deleteCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/auth/token")
                .maxAge(0) // expires immediately
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }
}
