package com.example.abonnements_service.service;

import com.example.abonnements_service.config.KafkaTopicConfig;
import com.example.abonnements_service.event.AbonnementEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for publishing events to Kafka
 * Handles asynchronous event publishing with error handling
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, AbonnementEvent> kafkaTemplate;

    /**
     * Publish abonnement event to Kafka
     * Uses utilisateurId as partition key for ordering guarantees (all events for same user go to same partition)
     */
    public void publishAbonnementEvent(AbonnementEvent event) {
        String key = event.getUtilisateurId().toString();  // Partition key
        String topic = KafkaTopicConfig.ABONNEMENT_EVENTS_TOPIC;

        log.info("Publishing event {} for user {} to Kafka topic {}",
                event.getEventType(), event.getUtilisateurId(), topic);

        CompletableFuture<SendResult<String, AbonnementEvent>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event {} for user {}: {}",
                        event.getEventType(), event.getUtilisateurId(), ex.getMessage(), ex);
            } else {
                log.info("Successfully published event {} for user {} to partition {} with offset {}",
                        event.getEventType(),
                        event.getUtilisateurId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
