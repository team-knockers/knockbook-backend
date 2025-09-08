package com.knockbook.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JSON deserialization
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RegisterEmailRequest {
    @NotBlank
    @Email
    private String email;
}
