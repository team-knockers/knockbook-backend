package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BookReadCountStat {
    private Integer yearAt;
    private Integer monthAt;
    private Integer readCountByMe;
    private Double avgReadCountByMember;
}
