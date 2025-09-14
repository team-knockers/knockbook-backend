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
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@Component
public class AccessTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JWTComponent jwtComponent;

    public AccessTokenFilter(JWTComponent jwtComponent) {
        this.jwtComponent = jwtComponent;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        final var header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response); // 익명으로 진행
            return;
        }

        final var jws = header.substring(7);

        try {
            final var claims = jwtComponent.parseJWS(jws, JWTComponent.Audience.ACCESS_TOKEN_HANDLER);
            final var subject = claims.getSubject();

            final var auth = new UsernamePasswordAuthenticationToken(subject, null, List.of());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(request, response);
        } catch (ParseException | JOSEException e) {
            SecurityContextHolder.clearContext();
            writeProblem401(response, request);
        }
    }

    private void writeProblem401(HttpServletResponse response,
                                 HttpServletRequest request)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        final var body = """
        {
          "type": "about:blank",
          "title": "Unauthorized",
          "status": 401,
          "detail": "Invalid or expired access token",
          "instance": "%s"
        }
        """.formatted(request.getRequestURI());

        response.getWriter().write(body);
        response.getWriter().flush();
    }
}

