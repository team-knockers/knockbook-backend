package com.knockbook.backend.dto;

import com.knockbook.backend.domain.UserAddress;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AddressResponse {
    @NotNull
    private String id;
    @NotNull
    private String userId;
    private String label;
    @NotNull
    private String recipientName;
    @NotNull
    private String phone;
    @NotNull
    private String postalCode;
    @NotNull
    private String address1;
    private String address2;
    private String entryInfo;
    private String deliveryMemo;
    @NotNull
    private Boolean isDefault;

    public static AddressResponse fromDomain(UserAddress domain) {
        return AddressResponse.builder()
                .id(domain.getId().toString())
                .userId(domain.getUserId().toString())
                .label(domain.getLabel())
                .recipientName(domain.getRecipientName())
                .phone(domain.getPhone())
                .postalCode(domain.getPostalCode())
                .address1(domain.getAddress1())
                .address2(domain.getAddress2())
                .entryInfo(domain.getEntryInfo())
                .deliveryMemo(domain.getDeliveryMemo())
                .isDefault(Boolean.TRUE.equals(domain.getIsDefault()))
                .build();
    }
}
