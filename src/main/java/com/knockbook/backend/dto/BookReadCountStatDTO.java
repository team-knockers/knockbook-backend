package com.knockbook.backend.dto;

import com.knockbook.backend.domain.BookReadCountStat;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BookReadCountStatDTO {
    private Integer yearAt;
    private Integer monthAt;
    private Integer readCountByMe;
    private Double avgReadCountByMember;

    public static BookReadCountStatDTO fromDomain(final BookReadCountStat domain) {
        return BookReadCountStatDTO.builder()
                .yearAt(domain.getYearAt())
                .monthAt(domain.getMonthAt())
                .readCountByMe(domain.getReadCountByMe())
                .avgReadCountByMember(domain.getAvgReadCountByMember())
                .build();
    }
}
