package com.knockbook.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.crypto.argon2.Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8;

@Configuration
public class PasswordConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        String idForEncode = "argon2"; // 기본 인코더
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("argon2", defaultsForSpringSecurity_v5_8());
        encoders.put("bcrypt", new BCryptPasswordEncoder());

        return new org.springframework.security.crypto.password.DelegatingPasswordEncoder(idForEncode, encoders);
    }
}
