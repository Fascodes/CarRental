package dev.fascodes.carRental.notification.controller;

import dev.fascodes.carRental.common.security.AuthenticatedUser;
import dev.fascodes.carRental.notification.dto.NotificationResponse;
import dev.fascodes.carRental.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(notificationService.getMyNotifications(user.email()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(notificationService.markAsRead(id, user.email()));
    }
}
