package com.knockbook.backend.controller;

import com.knockbook.backend.dto.AddressResponse;
import com.knockbook.backend.dto.CreateAddressRequest;
import com.knockbook.backend.dto.UpdateAddressRequest;
import com.knockbook.backend.service.UserAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/addresses")
public class UserAddressController {

    private final UserAddressService service;

    @GetMapping
    @PreAuthorize("#userId.toString() == authentication.name")
    public ResponseEntity<List<AddressResponse>> list(
            @PathVariable String userId) {
        final var body = service.getList(Long.valueOf(userId))
                .stream()
                .map(AddressResponse::fromDomain).toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{addressId}")
    @PreAuthorize("#userId.toString() == authentication.name")
    public ResponseEntity<AddressResponse> get(
            @PathVariable String userId,
            @PathVariable Long addressId) {
        final var domain = service.get(Long.valueOf(userId), addressId);
        final var body = AddressResponse.fromDomain(domain);
        return ResponseEntity.ok(body);
    }

    @PostMapping
    @PreAuthorize("#userId.toString() == authentication.name")
    public ResponseEntity<AddressResponse> create(
            @PathVariable String userId,
            @Valid @RequestBody CreateAddressRequest req) {
        final var userIdLong = Long.valueOf(userId);
        final var domain = req.toDomain(userIdLong);
        final var setDefault = Boolean.TRUE.equals(req.getSetAsDefault());
        final var saved = service.create(userIdLong, domain, setDefault);
        final var body = AddressResponse.fromDomain(saved);
        final var location = URI.create(String.format("/users/%d/addresses/%d", userIdLong, saved.getId()));
        return ResponseEntity.created(location).body(body); // 201
    }

    @PatchMapping("/{addressId}")
    @PreAuthorize("#userId.toString() == authentication.name")
    public ResponseEntity<Void> update(
            @PathVariable String userId,
            @PathVariable String addressId,
            @Valid @RequestBody UpdateAddressRequest req) {

        final var patch = req.toPatch(Long.valueOf(addressId), Long.valueOf(userId));
        service.update(patch);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204
    }

    @PostMapping("/{addressId}/make-default")
    @PreAuthorize("#userId.toString() == authentication.name")
    public ResponseEntity<Void> makeDefault(
            @PathVariable String userId,
            @PathVariable String addressId) {
        service.makeDefault(Long.valueOf(userId), Long.valueOf(addressId));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204
    }

    @DeleteMapping("/{addressId}")
    @PreAuthorize("#userId.toString() == authentication.name")
    public ResponseEntity<Void> delete(
            @PathVariable String userId,
            @PathVariable String addressId) {
        service.delete(Long.valueOf(userId), Long.valueOf(addressId));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204
    }
}
