package com.knockbook.backend.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CustomerQnaFileResponse {
    private String fileUrl;
    private String fileName;
    private String fileSize;
    private String fileType;
}
