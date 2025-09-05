package com.knockbook.backend.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class EmailVerificationService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private OctetSequenceKey jweSecret;

    public String issueAndSend(final String email, final Duration validPeriod) throws JOSEException {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email required");
        }

        // create 6-digit code
        final var code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));

        // calculate expiration
        final var exp = Instant.now().plus(validPeriod);

        // JWT claim (code, iss, exp, sub)
        final var issuer = "knockbook";
        final var claims = new JWTClaimsSet.Builder()
                .subject(email)
                .issuer(issuer)
                .expirationTime(Date.from(exp))
                .claim("code", code)
                .build();

        // create JWE (symmetric key: dir + A256GCM)
        final var header = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
                .contentType("JWT")
                .keyID(jweSecret.getKeyID())
                .build();

        final var jwe = new EncryptedJWT(header, claims);
        jwe.encrypt(new DirectEncrypter(jweSecret.toByteArray()));
        final var verificationToken = jwe.serialize();

        // send code via email
        sendCodeMail(email, code, validPeriod);

        return verificationToken;
    }

    private void sendCodeMail(final String email, final String code, final Duration validPeriod) {
        final var msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("[문앞의책방] 이메일 인증 코드");
        msg.setText("아래 6자리 인증 코드를 입력해 주세요:\n> %s\n\n유효 시간: %d분\n".formatted(code, validPeriod.toMinutes()));
        mailSender.send(msg);
    }
}
