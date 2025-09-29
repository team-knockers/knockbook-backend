package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CustomerQna {

    public enum Status { PENDING, ANSWERED, CLOSED }

    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Status status;
    private String answer;
    private Instant answeredAt;
    private Instant createdAt;
    private Instant updatedAt;
    private List<CustomerQnaFile> files;
}
