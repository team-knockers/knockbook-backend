package com.knockbook.backend.config;

import com.knockbook.backend.component.JWTComponent;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@Component
public class AccessTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JWTComponent jwtComponent;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        final var header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            final var jws = header.substring(7);
            final var audience = JWTComponent.Audience.ACCESS_TOKEN_HANDLER;
            try {
                final var claims = jwtComponent.parseJWS(jws, audience);
                final var subject = claims.getSubject();
                final var auth = new UsernamePasswordAuthenticationToken(subject, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (ParseException | JOSEException e) {
                throw new RuntimeException(e);
            }
        }
        filterChain.doFilter(request, response);
    }
}
