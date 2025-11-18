package com.example.abonnements_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka topic configuration
 * Creates topics automatically on startup
 */
@Configuration
public class KafkaTopicConfig {

    public static final String ABONNEMENT_EVENTS_TOPIC = "abonnement.events";

    /**
     * Main topic for all subscription events
     * Other services (Notifications, Analytics) will subscribe to this topic
     */
    @Bean
    public NewTopic abonnementEventsTopic() {
        return TopicBuilder.name(ABONNEMENT_EVENTS_TOPIC)
                .partitions(3)  // 3 partitions for scalability
                .replicas(1)    // 1 replica (increase in production)
                .compact()      // Log compaction for event sourcing
                .build();
    }
}
