package com.knockbook.backend.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CustomerQnaPageResponse {
    private List<CustomerQnaResponse> qnas;
    private long totalQnas;
}
