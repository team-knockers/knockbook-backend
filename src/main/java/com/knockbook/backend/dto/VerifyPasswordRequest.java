package com.knockbook.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class VerifyPasswordRequest {
    @NotBlank
    @Size(min = 8, max = 12)
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&+=])[A-Za-z\\d@$!%*?&+=]{8,12}$",
            message = "Password must be 8-12 characters long and include at least one letter, one digit," +
                    " and one special character (@$!%*?&)."
    )
    private String password;
}
