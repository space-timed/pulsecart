package com.pulsecart.order.outbox;

import com.pulsecart.order.domain.OutboxEvent;
import com.pulsecart.order.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(name = "pulsecart.outbox.poller.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OutboxEventRepository outboxEventRepository,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepository.findTop50ByPublishedFalseOrderByCreatedAtAsc();
        if (pending.isEmpty()) {
            return;
        }

        log.debug("Found {} unpublished outbox events in order-service", pending.size());
        for (OutboxEvent event : pending) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                event.setPublished(true);
                                event.setPublishedAt(Instant.now());
                                outboxEventRepository.save(event);
                                log.info("Successfully published outbox event {} to topic {}", event.getId(), event.getTopic());
                            } else {
                                log.warn("Failed to publish outbox event {}: {}", event.getId(), ex.getMessage());
                            }
                        });
            } catch (Exception e) {
                log.warn("Error sending outbox event {} to Kafka: {}", event.getId(), e.getMessage());
            }
        }
    }
}
