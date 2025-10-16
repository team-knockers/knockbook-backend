package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class KakaoReadyInfo {
    private final String tid;
    private final String nextRedirectPcUrl;
    private final String nextRedirectMobileUrl;
    private final String nextRedirectAppUrl;
    private final Integer amount;
    private final Long orderId;
    private final Long userId;
}
