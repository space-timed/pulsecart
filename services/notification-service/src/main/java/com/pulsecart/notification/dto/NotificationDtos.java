package com.pulsecart.notification.dto;

import com.pulsecart.notification.domain.NotificationChannel;
import com.pulsecart.notification.domain.NotificationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

public final class NotificationDtos {

    private NotificationDtos() {}

    public record SendNotificationRequest(
            @NotBlank String recipient,
            @NotNull NotificationChannel channel,
            @NotBlank String templateName,
            String subject,
            Map<String, String> variables,
            String orderId
    ) {}

    public record NotificationResponse(
            String id,
            String recipient,
            NotificationChannel channel,
            String templateName,
            String subject,
            String content,
            NotificationStatus status,
            String orderId,
            Instant createdAt
    ) {}

    public record DlqMessageDto(
            Long id,
            String topic,
            int partitionId,
            long offsetVal,
            String payload,
            String exceptionMessage,
            boolean replayed,
            Instant failedAt
    ) {}
}
