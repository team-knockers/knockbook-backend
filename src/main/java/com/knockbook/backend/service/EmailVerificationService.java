package com.knockbook.backend.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
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

    private enum Audiences {
        EMAIL_VERIFICATION_HANDLER,
        EMAIL_REGISTRATION_HANDLER,
    }

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private OctetSequenceKey jweSecret;

    private final String issuer = "knockbook";

    public String sendCodeAndIssueVerificationToken(final String email,
                                                    final Duration validPeriod)
            throws JOSEException {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email required");
        }

        // create 6-digit code
        final var rawCode = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        final var code = String.format("%06d", rawCode);
        final var verificationToken = issueVerificationToken(email, code, validPeriod);
        // send code via email
        sendCodeMail(email, code, validPeriod);
        return verificationToken;
    }

    public String verifyAndIssueRegistrationToken(final String emailVerificationToken,
                                                  final String code,
                                                  final Duration validPeriod)
            throws ParseException, JOSEException {
        // decrypt JWE (emailVerificationToken)
        final var jwe = EncryptedJWT.parse(emailVerificationToken);
        jwe.decrypt(new DirectDecrypter(jweSecret.toByteArray()));
        final var verificationClaims = jwe.getJWTClaimsSet();
        verifyVerificationToken(verificationClaims, code);
        final var email = verificationClaims.getSubject();
        return issueRegistrationToken(email, validPeriod);
    }

    private String issueVerificationToken(final String email,
                                          final String code,
                                          final Duration validPeriod)
            throws JOSEException {
        // calculate expiration
        final var expirationTime = Instant.now().plus(validPeriod);

        // JWT claim (code, iss, exp, sub)
        final var claims = new JWTClaimsSet.Builder()
                .subject(email)
                .issuer(issuer)
                .expirationTime(Date.from(expirationTime))
                .audience(Audiences.EMAIL_VERIFICATION_HANDLER.toString())
                .claim("code", code)
                .build();

        // create JWE (symmetric key: dir + A256GCM)
        final var header = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
                .contentType("JWT")
                .keyID(jweSecret.getKeyID())
                .build();

        final var jwe = new EncryptedJWT(header, claims);
        jwe.encrypt(new DirectEncrypter(jweSecret.toByteArray()));
        return jwe.serialize();
    }
    private void sendCodeMail(final String email, final String code, final Duration validPeriod) {
        final var msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("[문앞의책방] 이메일 인증 코드");
        msg.setText("아래 6자리 인증 코드를 입력해 주세요:\n> %s\n\n유효 시간: %d분\n".formatted(code, validPeriod.toMinutes()));
        mailSender.send(msg);
    }
    private void verifyVerificationToken(final JWTClaimsSet claims, final String code) {
        // expiration check
        if (claims.getExpirationTime() == null ||
                Instant.now().isAfter(claims.getExpirationTime().toInstant())) {
            throw new IllegalArgumentException("verification code expired");
        }

        // ensure audience matches
        final var audiences = claims.getAudience();
        if (!audiences.contains(Audiences.EMAIL_VERIFICATION_HANDLER.toString())) {
            throw new IllegalArgumentException("invalid audience");
        }

        // ensure code matches
        final var expected = (String)claims.getClaim("code");
        if (!code.equals(expected)) {
            throw new IllegalArgumentException("invalid verification code");
        }
    }
    private String issueRegistrationToken(final String email, final Duration validPeriod) throws JOSEException {
        // issue JWS (HS256)
        final var now = Instant.now();
        final var expirationTime = now.plus(validPeriod); // registration token TTL
        final var registrationClaims = new JWTClaimsSet.Builder()
                .subject(email)
                .issuer(issuer)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expirationTime))
                .audience(Audiences.EMAIL_REGISTRATION_HANDLER.toString())
                .build();

        final var jwsHeader = new JWSHeader.Builder(JWSAlgorithm.HS256)
                .type(JOSEObjectType.JWT)
                .keyID(jweSecret.getKeyID())
                .build();

        final var jws = new SignedJWT(jwsHeader, registrationClaims);
        jws.sign(new MACSigner(jweSecret.toByteArray()));
        return jws.serialize(); // return registration token (compact jws)
    }
}
