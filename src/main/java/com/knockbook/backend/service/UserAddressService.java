package com.knockbook.backend.service;

import com.knockbook.backend.domain.UserAddress;
import com.knockbook.backend.exception.AddressNotBelongToUserException;
import com.knockbook.backend.exception.UserAddressNotFoundException;
import com.knockbook.backend.repository.UserAddressRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserAddressRepository repository;

    public List<UserAddress> getList(final Long userId) {
        return repository.findByUserId(userId);
    }

    public UserAddress get(final Long userId,
                           final Long addressId) {
        final var found = repository.findById(addressId)
                .orElseThrow(() -> new UserAddressNotFoundException(addressId));
        if (!found.getUserId().equals(userId)) {
            throw new AddressNotBelongToUserException(userId);
        }
        return found;
    }

    @Transactional
    public UserAddress create(final Long userId,
                              final UserAddress payload,
                              final boolean setAsDefault) {
        var toSave = UserAddress.builder()
                .userId(userId)
                .label(payload.getLabel())
                .recipientName(payload.getRecipientName())
                .phone(payload.getPhone())
                .postalCode(payload.getPostalCode())
                .address1(payload.getAddress1())
                .address2(payload.getAddress2())
                .entryInfo(payload.getEntryInfo())
                .deliveryMemo(payload.getDeliveryMemo())
                .isDefault(false)
                .build();

        final var saved = repository.insert(toSave);

        if (!setAsDefault) {
            return saved;
        }

        final var addressId = saved.getId();
        repository.setDefault(userId, addressId);
        return repository.findById(addressId)
                .orElseThrow(() -> new UserAddressNotFoundException(addressId));
    }

    public void update(final UserAddress patch) {
        final var current = get(patch.getUserId(), patch.getId());
        var merged = UserAddress.builder()
                .id(current.getId())
                .userId(current.getUserId())
                .label(patch.getLabel() != null
                        ? patch.getLabel() : current.getLabel())
                .recipientName(patch.getRecipientName() != null
                        ? patch.getRecipientName() : current.getRecipientName())
                .phone(patch.getPhone() != null
                        ? patch.getPhone() : current.getPhone())
                .postalCode(patch.getPostalCode() != null
                        ? patch.getPostalCode() : current.getPostalCode())
                .address1(patch.getAddress1() != null
                        ? patch.getAddress1() : current.getAddress1())
                .address2(patch.getAddress2() != null
                        ? patch.getAddress2() : current.getAddress2())
                .entryInfo(patch.getEntryInfo() != null
                        ? patch.getEntryInfo() : current.getEntryInfo())
                .deliveryMemo(patch.getDeliveryMemo() != null
                        ? patch.getDeliveryMemo() : current.getDeliveryMemo())
                .isDefault(current.getIsDefault())
                .build();

        repository.update(merged);
    }

    public void makeDefault(Long userId, Long addressId) {
        get(userId, addressId);
        repository.setDefault(userId, addressId);
    }

    public void delete(Long userId, Long addressId) {
        get(userId, addressId);
        repository.delete(addressId);
    }
}
