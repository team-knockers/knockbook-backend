package com.knockbook.backend.service;

import com.knockbook.backend.component.JWTComponent;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class LocalAuthService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private JWTComponent jwtComponent;

    public String sendCodeViaEmailAndIssueVerificationToken(final String email,
                                                            final Duration validPeriod)
            throws JOSEException, MessagingException {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email required");
        }

        // create 6-digit code
        final var code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        final var verificationToken = issueVerificationToken(email, code, validPeriod);
        sendAuthCodeViaEmail(email, code, validPeriod); // send code via email
        return verificationToken;
    }

    public String verifyEmailVerificationTokenAndIssueRegistrationToken(final String emailVerificationToken,
                                                                        final String code,
                                                                        final Duration validPeriod)
            throws ParseException, JOSEException {
        final var claims = jwtComponent.parseJWE(emailVerificationToken,
                JWTComponent.Audience.EMAIL_VERIFICATION_HANDLER);
        // ensure code matches
        final var expected = (String)claims.getClaim("code");
        if (!code.equals(expected)) {
            throw new IllegalArgumentException("invalid verification code");
        }
        final var email = claims.getSubject();
        return issueRegistrationToken(email, validPeriod);
    }

    public String getSubjectFromRegistrationToken(final String registrationToken)
            throws ParseException, JOSEException {
        // Parse & validate registration token (JWS)
        final var claims = jwtComponent.parseJWS(registrationToken,
                JWTComponent.Audience.LOCAL_REGISTRATION_HANDLER);
        return claims.getSubject(); // email
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
                .audience(JWTComponent.Audience.LOCAL_REGISTRATION_HANDLER.toString())
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

    private void sendAuthCodeViaEmail(final String email,
                                      final String code,
                                      final Duration validPeriod)
            throws MessagingException {
        final var message = mailSender.createMimeMessage();
        final var helper = new MimeMessageHelper(message, false, "UTF-8");

        helper.setFrom("noreply@knockbook.store"); // Postmark 인증된 발신자
        helper.setTo(email);
        helper.setSubject("[문앞의책방] 이메일 인증 코드");

        String body = """
                아래 6자리 인증 코드를 입력해 주세요:
                > %s

                유효 시간: %d분
                """.formatted(code, validPeriod.toMinutes());

        helper.setText(body, false);
        message.addHeader("X-PM-Message-Stream", "outbound");
        mailSender.send(message);
    }

}
