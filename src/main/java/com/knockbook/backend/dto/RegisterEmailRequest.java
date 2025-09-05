package com.knockbook.backend.dto;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JSON deserialization
@AllArgsConstructor
public class RegisterEmailRequest {
    private String email;
}
