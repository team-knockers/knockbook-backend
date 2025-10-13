package com.knockbook.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JSON deserialization
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CreateProductInquiryRequest {
    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    @Size(max = 300)
    private String questionBody;
}
