package com.knockbook.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CreateProductReviewRequest {
    @NotBlank
    @Size(max = 300)
    private String body;

    @NotNull
    @Min(1) @Max(5)
    private Integer rating;
}
