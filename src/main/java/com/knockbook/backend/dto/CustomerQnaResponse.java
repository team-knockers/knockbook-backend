package com.knockbook.backend.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CustomerQnaResponse {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String status;
    private String answer;
    private Instant answeredAt;
    private Instant createdAt;
    private List<CustomerQnaFileResponse> files;
}
