package com.knockbook.backend.dto;

import com.knockbook.backend.domain.UserAddress;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UpdateAddressRequest {
    private String recipientName;
    private String phone;
    private String postalCode;
    private String address1;
    private String address2;
    private String label;
    private String entryInfo;
    private String deliveryMemo;

    public UserAddress toPatch(Long addressId, Long userId) {
        return UserAddress.builder()
                .id(addressId)
                .userId(userId)
                .label(label)
                .recipientName(recipientName)
                .phone(phone)
                .postalCode(postalCode)
                .address1(address1)
                .address2(address2)
                .entryInfo(entryInfo)
                .deliveryMemo(deliveryMemo)
                .build();
    }
}
