package com.knockbook.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;

public class ProblemDetailFactory {

    public static ProblemDetail of(final HttpStatus status,
                                   final String title,
                                   final String detail,
                                   final String code,
                                   final String type) {
        final var p = ProblemDetail.forStatusAndDetail(status, detail);
        p.setTitle(title);
        if (type != null) { p.setType(URI.create(type)); }
        if (code != null) { p.setProperty("code", code); }
        return p;
    }

    public static ProblemDetail of(final HttpStatus status,
                                   final String title,
                                   final String detail) {
        return of(status, title, detail, null, null);
    }
}
