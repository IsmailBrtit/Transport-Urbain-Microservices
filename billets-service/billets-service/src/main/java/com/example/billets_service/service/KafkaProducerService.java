package com.example.billets_service.service;

import com.example.billets_service.event.TicketEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service de publication d'événements Kafka
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private static final String TICKET_TOPIC = "ticket.events";

    private final KafkaTemplate<String, TicketEvent> kafkaTemplate;

    public void publishTicketEvent(TicketEvent event) {
        try {
            // Utiliser utilisateurId comme clé de partition pour garantir l'ordre
            String partitionKey = event.getUtilisateurId().toString();

            kafkaTemplate.send(TICKET_TOPIC, partitionKey, event);

            log.info("Événement publié sur Kafka: {} (topic: {}, key: {})",
                    event.getEventType(), TICKET_TOPIC, partitionKey);
        } catch (Exception e) {
            log.error("Erreur lors de la publication sur Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Échec de publication Kafka", e);
        }
    }
}
