package com.knockbook.backend.entity;

import com.knockbook.backend.domain.UserAddress;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_addresses")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserAddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "label")
    private String label;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "address1", nullable = false)
    private String address1;

    @Column(name = "address2")
    private String address2;

    @Column(name = "entry_info")
    private String entryInfo;

    @Column(name = "delivery_memo")
    private String deliveryMemo;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public UserAddress toDomain() {
        return UserAddress.builder()
                .id(id)
                .userId(userId)
                .label(label)
                .recipientName(recipientName)
                .phone(phone)
                .postalCode(postalCode)
                .address1(address1)
                .address2(address2)
                .entryInfo(entryInfo)
                .deliveryMemo(deliveryMemo)
                .isDefault(Boolean.TRUE.equals(isDefault))
                .build();
    }

    public static UserAddressEntity fromDomain(UserAddress domain) {
        if (domain == null) { return null; }
        return UserAddressEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .label(domain.getLabel())
                .recipientName(domain.getRecipientName())
                .phone(domain.getPhone())
                .postalCode(domain.getPostalCode())
                .address1(domain.getAddress1())
                .address2(domain.getAddress2())
                .entryInfo(domain.getEntryInfo())
                .deliveryMemo(domain.getDeliveryMemo())
                .isDefault(Boolean.TRUE.equals(domain.getIsDefault())) // null -> false
                .build();
    }
}
