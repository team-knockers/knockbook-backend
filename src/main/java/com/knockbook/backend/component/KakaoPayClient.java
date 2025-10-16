package com.knockbook.backend.component;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KakaoPayClient {

    private final RestTemplate restTemplate;
    private final KakaoPayProps props;

    private HttpHeaders headers() {
        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "SECRET_KEY " + props.getSecretKey());
        return headers;
    }

    public Map<String, Object> ready(Map<String, Object> body) {
        final var res = restTemplate.exchange(
                props.getApiBase() + "/online/v1/payment/ready",
                HttpMethod.POST,
                new HttpEntity<>(body, headers()),
                Map.class
        );
        return res.getBody();
    }

    public Map<String, Object> approve(Map<String, Object> body) {
        final var res = restTemplate.exchange(
                props.getApiBase() + "/online/v1/payment/approve",
                HttpMethod.POST,
                new HttpEntity<>(body, headers()),
                Map.class
        );
        return res.getBody();
    }
}
