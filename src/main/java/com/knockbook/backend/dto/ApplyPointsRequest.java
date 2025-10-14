package com.knockbook.backend.dto;

import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ApplyPointsRequest {
    @NonNull @Min(0)
    private Integer points;
}
