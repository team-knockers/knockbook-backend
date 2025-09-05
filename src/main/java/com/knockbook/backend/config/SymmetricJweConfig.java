package com.knockbook.backend.config;

import com.nimbusds.jose.jwk.OctetSequenceKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;
import java.util.UUID;

@Configuration
public class SymmetricJweConfig {
    @Bean
    public OctetSequenceKey jweSecret() {
        final var key = new byte[32]; // 256-bit
        new SecureRandom().nextBytes(key);
        return new OctetSequenceKey.Builder(key)
                .keyID("boot-" + UUID.randomUUID()) // 로그/외부노출 금지
                .build();
    }
}