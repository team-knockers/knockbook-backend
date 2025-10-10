package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserAddress {
    private Long id;
    private Long userId;
    private String label;
    private String recipientName;
    private String phone;
    private String postalCode;
    private String address1;
    private String address2;
    private String entryInfo;
    private String deliveryMemo;
    private Boolean isDefault;
}
