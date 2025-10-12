package com.knockbook.backend.repository;

import com.knockbook.backend.domain.UserAddress;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository {
    UserAddress insert(UserAddress address);
    void update(UserAddress patch);
    Optional<UserAddress> findById(Long id);
    List<UserAddress> findByUserId(Long userId);
    Optional<UserAddress> findDefaultByUserId(Long userId);
    void setDefault(Long userId, Long addressId);
    void delete(Long id);
}
