package com.knockbook.backend.dto;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JSON deserialization
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RegisterEmailRequest {
    private String email;
}
