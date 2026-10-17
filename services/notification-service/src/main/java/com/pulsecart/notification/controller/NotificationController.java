package com.pulsecart.notification.controller;

import com.pulsecart.notification.dto.NotificationDtos.DlqMessageDto;
import com.pulsecart.notification.dto.NotificationDtos.NotificationResponse;
import com.pulsecart.notification.dto.NotificationDtos.SendNotificationRequest;
import com.pulsecart.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(@Valid @RequestBody SendNotificationRequest request) {
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestParam(required = false) String recipient,
            @RequestParam(required = false) String orderId
    ) {
        if (orderId != null && !orderId.isBlank()) {
            return ResponseEntity.ok(notificationService.getNotificationsByOrderId(orderId));
        }
        if (recipient != null && !recipient.isBlank()) {
            return ResponseEntity.ok(notificationService.getNotificationsByRecipient(recipient));
        }
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/dlq")
    public ResponseEntity<List<DlqMessageDto>> getPendingDlqMessages() {
        return ResponseEntity.ok(notificationService.getPendingDlqMessages());
    }

    @PostMapping("/dlq/{id}/replay")
    public ResponseEntity<Void> replayDlqMessage(@PathVariable Long id) {
        boolean success = notificationService.replayDlqMessage(id);
        if (success) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
