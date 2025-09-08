package com.knockbook.backend.component;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;

@Component
public class JWTComponent {

    public enum Audience {
        EMAIL_VERIFICATION_HANDLER,
        EMAIL_REGISTRATION_HANDLER,
        ACCESS_TOKEN_REFRESH_HANDLER,
        ACCESS_TOKEN_HANDLER,
    }

    @Autowired
    @Qualifier("jwsSecret")
    private OctetSequenceKey jwsSecret;

    @Autowired
    @Qualifier("jweSecret")
    private OctetSequenceKey jweSecret;

    public static final String issuer = "knockbook";

    public String issueJWE(final JWTClaimsSet claims)
            throws JOSEException {
        // symmetric key: dir + A256GCM
        final var header = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
                .contentType("JWT")
                .keyID(jweSecret.getKeyID())
                .build();

        final var jwe = new EncryptedJWT(header, claims);
        jwe.encrypt(new DirectEncrypter(jweSecret.toByteArray()));
        return jwe.serialize();
    }

    public String issueJWS(final JWTClaimsSet claims)
            throws JOSEException {
        final var header = new JWSHeader.Builder(JWSAlgorithm.HS256)
                .type(JOSEObjectType.JWT)
                .keyID(jweSecret.getKeyID())
                .build();

        final var jws = new SignedJWT(header, claims);
        jws.sign(new MACSigner(jweSecret.toByteArray()));
        return jws.serialize();
    }

    public JWTClaimsSet parseJWE(final String jwe,
                                 final Audience audience)
            throws ParseException, JOSEException {
        final var parsed = EncryptedJWT.parse(jwe);
        parsed.decrypt(new DirectDecrypter(jweSecret.toByteArray()));
        final var claims = parsed.getJWTClaimsSet();
        verifyClaims(claims, audience);
        return claims;
    }

    public JWTClaimsSet parseJWS(final String jws,
                                 final Audience audience)
            throws ParseException, JOSEException {
        final var parsed = SignedJWT.parse(jws);
        parsed.verify(new MACVerifier(jwsSecret.toByteArray()));
        final var claims = parsed.getJWTClaimsSet();
        verifyClaims(claims, audience);
        return claims;
    }

    private void verifyClaims(final JWTClaimsSet claims,
                              final Audience audience) {
        // expiration check
        if (claims.getExpirationTime() == null ||
                Instant.now().isAfter(claims.getExpirationTime().toInstant())) {
            throw new IllegalArgumentException("verification code expired");
        }

        // ensure audience matches
        final var audiences = claims.getAudience();
        if (!audiences.contains(audience.toString())) {
            throw new IllegalArgumentException("invalid audiences");
        }
    }
}
