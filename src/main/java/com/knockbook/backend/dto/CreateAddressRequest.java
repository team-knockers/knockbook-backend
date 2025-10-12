package com.knockbook.backend.dto;

import com.knockbook.backend.domain.UserAddress;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CreateAddressRequest {
    @NotBlank
    private String recipientName;
    @NotBlank
    private String phone;
    @NotBlank
    private String postalCode;
    @NotBlank
    private String address1;
    private String address2;
    private String label;
    private String entryInfo;
    private String deliveryMemo;
    private Boolean isDefault;

    public UserAddress toDomain(Long userId) {
        return UserAddress.builder()
                .userId(userId)
                .label(label)
                .recipientName(recipientName)
                .phone(phone)
                .postalCode(postalCode)
                .address1(address1)
                .address2(address2)
                .entryInfo(entryInfo)
                .deliveryMemo(deliveryMemo)
                .isDefault(isDefault)
                .build();
    }
}
