package com.knockbook.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Autowired
    private AccessTokenFilter accessTokenFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/local/**").permitAll()
                        .requestMatchers("/auth/token/**").permitAll()
                        .requestMatchers("/checkout/kakao/**","/error").permitAll()
                        .requestMatchers("/db-ping").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(accessTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                    .authenticationEntryPoint((req,res,ex)-> res.sendError(401))
                    .accessDeniedHandler((req,res,ex)-> res.sendError(403))
                ).build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final var corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "https://knockbook-1a9b7.web.app",
                "https://knockbook.store"
        ));
        corsConfig.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        final var src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", corsConfig);
        return src;
    }
}
