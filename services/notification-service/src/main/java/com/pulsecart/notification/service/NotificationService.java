package com.pulsecart.notification.service;

import com.pulsecart.notification.domain.DlqMessage;
import com.pulsecart.notification.domain.Notification;
import com.pulsecart.notification.domain.NotificationChannel;
import com.pulsecart.notification.domain.NotificationStatus;
import com.pulsecart.notification.dto.NotificationDtos.DlqMessageDto;
import com.pulsecart.notification.dto.NotificationDtos.NotificationResponse;
import com.pulsecart.notification.dto.NotificationDtos.SendNotificationRequest;
import com.pulsecart.notification.repository.DlqMessageRepository;
import com.pulsecart.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final DlqMessageRepository dlqMessageRepository;

    private static final Map<String, String> TEMPLATES = Map.of(
            "ORDER_CONFIRMED", "Hello {{customerName}}, your order {{orderId}} of ${{totalAmount}} has been confirmed and is being prepared!",
            "ORDER_CANCELLED", "Hello {{customerName}}, your order {{orderId}} has been cancelled. Any authorizations have been released.",
            "SHIPMENT_DISPATCHED", "Good news {{customerName}}! Your order {{orderId}} has shipped via {{carrier}}. Tracking: {{trackingNumber}}.",
            "PAYMENT_FAILED", "Notice: Payment for your order {{orderId}} could not be processed. Please check your payment details."
    );

    public NotificationService(NotificationRepository notificationRepository, DlqMessageRepository dlqMessageRepository) {
        this.notificationRepository = notificationRepository;
        this.dlqMessageRepository = dlqMessageRepository;
    }

    @Transactional
    public NotificationResponse sendNotification(SendNotificationRequest request) {
        String content = renderTemplate(request.templateName(), request.variables());
        String notifId = "notif-" + UUID.randomUUID().toString().substring(0, 8);

        Notification notification = new Notification(
                notifId,
                request.recipient(),
                request.channel() != null ? request.channel() : NotificationChannel.EMAIL,
                request.templateName(),
                request.subject() != null ? request.subject() : "PulseCart Update: " + request.templateName(),
                content,
                NotificationStatus.SENT,
                request.orderId()
        );

        Notification saved = notificationRepository.save(notification);
        log.info("Notification sent [id: {}, channel: {}, recipient: {}, orderId: {}]",
                saved.getId(), saved.getChannel(), saved.getRecipient(), saved.getOrderId());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByRecipient(String recipient) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByOrderId(String orderId) {
        return notificationRepository.findByOrderId(orderId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DlqMessage recordDlqMessage(String topic, int partition, long offset, String payload, String error) {
        log.warn("Recording Dead-Letter message on topic {} [partition: {}, offset: {}]: {}", topic, partition, offset, error);
        DlqMessage dlqMessage = new DlqMessage(topic, partition, offset, payload, error);
        return dlqMessageRepository.save(dlqMessage);
    }

    @Transactional(readOnly = true)
    public List<DlqMessageDto> getPendingDlqMessages() {
        return dlqMessageRepository.findByReplayedFalseOrderByFailedAtDesc().stream()
                .map(m -> new DlqMessageDto(
                        m.getId(),
                        m.getTopic(),
                        m.getPartitionId(),
                        m.getOffsetVal(),
                        m.getPayload(),
                        m.getExceptionMessage(),
                        m.isReplayed(),
                        m.getFailedAt()
                ))
                .toList();
    }

    @Transactional
    public boolean replayDlqMessage(Long id) {
        Optional<DlqMessage> optional = dlqMessageRepository.findById(id);
        if (optional.isPresent()) {
            DlqMessage msg = optional.get();
            msg.setReplayed(true);
            dlqMessageRepository.save(msg);
            log.info("DLQ message id {} marked as replayed", id);
            return true;
        }
        return false;
    }

    public String renderTemplate(String templateName, Map<String, String> variables) {
        String template = TEMPLATES.getOrDefault(templateName, "Notification regarding {{orderId}}: template {{templateName}}");
        if (variables == null || variables.isEmpty()) {
            return template.replace("{{templateName}}", templateName);
        }

        String rendered = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}", entry.getValue() != null ? entry.getValue() : "");
        }
        return rendered;
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getRecipient(),
                n.getChannel(),
                n.getTemplateName(),
                n.getSubject(),
                n.getContent(),
                n.getStatus(),
                n.getOrderId(),
                n.getCreatedAt()
        );
    }
}
