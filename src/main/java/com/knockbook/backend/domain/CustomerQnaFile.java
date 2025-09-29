package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CustomerQnaFile {
    private Long id;
    private String fileUrl;
    private String fileName;
    private long fileSize;
    private String fileType;
}
