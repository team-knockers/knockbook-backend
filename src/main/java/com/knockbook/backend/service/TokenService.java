package com.knockbook.backend.service;

import com.knockbook.backend.component.JWTComponent;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class TokenService {

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder
    public static class TokenResult {
        private final String accessToken;
        private final String refreshToken;
    }

    @Autowired
    private JWTComponent jwtComponent;

    public static final Duration accessTokenValidPeriod = Duration.ofMinutes(10);
    public static final Duration refreshTokenValidPeriod = Duration.ofHours(12);
    public static final String refreshTokenCookieName = "refresh_token";

    public TokenResult issueTokens(final String subject)
            throws JOSEException {
        return TokenResult.builder()
                .accessToken(issueAccessToken(subject))
                .refreshToken(issueRefreshToken(subject))
                .build();
    }

    public TokenResult refreshTokens(final String refreshToken)
            throws ParseException, JOSEException {
        final var audience = JWTComponent.Audience.REFRESH_TOKEN_HANDLER;
        final var claims = jwtComponent.parseJWS(refreshToken, audience);
        final var subject = claims.getSubject();
        return TokenResult.builder()
                .accessToken(issueAccessToken(subject))
                .refreshToken(issueRefreshToken(subject))
                .build();
    }

    public void verifyAccessToken(final String accessToken,
                                  final String subject)
            throws ParseException, JOSEException {
        final var audience = JWTComponent.Audience.ACCESS_TOKEN_HANDLER;
        final var claims = jwtComponent.parseJWS(accessToken, audience);
        if (!subject.equals(claims.getSubject())) {
            throw new IllegalArgumentException("invalid subject");
        }
    }

    private String issueAccessToken(final String subject) throws JOSEException {
        final var now = Instant.now();
        final var expirationTime = now.plus(accessTokenValidPeriod);
        final var accessClaims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer(JWTComponent.issuer)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expirationTime))
                .audience(JWTComponent.Audience.ACCESS_TOKEN_HANDLER.toString())
                .build();
        return jwtComponent.issueJWS(accessClaims);
    }

    private String issueRefreshToken(final String subject) throws JOSEException {
        final var now = Instant.now();
        final var expirationTime = now.plus(refreshTokenValidPeriod);
        final var refreshClaims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer(JWTComponent.issuer)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expirationTime))
                .audience(JWTComponent.Audience.REFRESH_TOKEN_HANDLER.toString())
                .build();
        return jwtComponent.issueJWS(refreshClaims);
    }
}
