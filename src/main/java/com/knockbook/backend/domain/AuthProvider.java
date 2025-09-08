package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AuthProvider {

    public enum Kind { oauth, local, oidc }

    private String providerCode;
    private String displayName;
    private Kind kind;
    private Boolean isEnabled;
    private String iconUrl;
}
