package com.knockbook.backend.service;

import com.knockbook.backend.component.JWTComponent;
import com.nimbusds.jose.*;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class EmailVerificationService {

    @Autowired
    private JWTComponent jwtComponent;

    @Autowired
    private JavaMailSender mailSender;

    public String sendCodeAndIssueVerificationToken(final String email,
                                                    final Duration validPeriod)
            throws JOSEException {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email required");
        }

        // create 6-digit code
        final var code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        final var verificationToken = issueVerificationToken(email, code, validPeriod);
        sendCodeMail(email, code, validPeriod); // send code via email
        return verificationToken;
    }

    public String verifyAndIssueRegistrationToken(final String emailVerificationToken,
                                                  final String code,
                                                  final Duration validPeriod)
            throws ParseException, JOSEException {
        final var claims = jwtComponent.parseJWE(emailVerificationToken,
                JWTComponent.Audience.EMAIL_VERIFICATION_HANDLER);
        verifyVerificationToken(claims, code);
        final var email = claims.getSubject();
        return issueRegistrationToken(email, validPeriod);
    }

    private String issueRegistrationToken(final String email,
                                          final Duration validPeriod)
            throws JOSEException {
        final var now = Instant.now();
        final var expirationTime = now.plus(validPeriod); // registration token TTL
        final var claims = new JWTClaimsSet.Builder()
                .subject(email)
                .issuer(JWTComponent.issuer)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expirationTime))
                .audience(JWTComponent.Audience.EMAIL_REGISTRATION_HANDLER.toString())
                .build();

        return jwtComponent.issueJWS(claims);
    }

    private String issueVerificationToken(final String email,
                                         final String code,
                                         final Duration validPeriod)
            throws JOSEException {
        final var expirationTime = Instant.now().plus(validPeriod);
        final var claims = new JWTClaimsSet.Builder()
                .subject(email)
                .issuer(JWTComponent.issuer)
                .expirationTime(Date.from(expirationTime))
                .audience(JWTComponent.Audience.EMAIL_VERIFICATION_HANDLER.toString())
                .claim("code", code)
                .build();

        return jwtComponent.issueJWE(claims);
    }

    private void sendCodeMail(final String email, final String code, final Duration validPeriod) {
        final var msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("[문앞의책방] 이메일 인증 코드");
        msg.setText("아래 6자리 인증 코드를 입력해 주세요:\n> %s\n\n유효 시간: %d분\n".formatted(code, validPeriod.toMinutes()));
        mailSender.send(msg);
    }

    private void verifyVerificationToken(final JWTClaimsSet claims,
                                           final String code)
            throws JOSEException, ParseException {

        // ensure code matches
        final var expected = (String)claims.getClaim("code");
        if (!code.equals(expected)) {
            throw new IllegalArgumentException("invalid verification code");
        }
    }
}
