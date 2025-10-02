package com.knockbook.backend.controller;

import com.knockbook.backend.dto.GetNotificationsResponse;
import com.knockbook.backend.dto.NotificationDto;
import com.knockbook.backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/customers")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PreAuthorize("#userId == authentication.name")
    @GetMapping(path = "/{userId}/notification")
    public ResponseEntity<GetNotificationsResponse> getNotifications(
            @PathVariable("userId") final String userId,
            @RequestParam(name = "page") final int page,
            @RequestParam(name = "size") final int size) {

        final var zeroBasePage = Math.max(page - 1, 0);
        final var slice = notificationService.getAll(zeroBasePage, size);
        final var dtos = slice.items()
                .stream()
                .map(i -> NotificationDto.builder()
                        .id(i.getId().toString())
                        .title(i.getTitle())
                        .content(i.getContent())
                        .createdAt(i.getCreatedAt().toString())
                        .build()
                ).toList();

        final var body = GetNotificationsResponse.builder()
                .notifications(dtos)
                .totalCounts(slice.total())
                .build();

        return ResponseEntity.ok().body(body);
    }
}
