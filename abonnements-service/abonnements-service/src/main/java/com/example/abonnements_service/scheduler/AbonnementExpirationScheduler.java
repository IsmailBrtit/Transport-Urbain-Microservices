package com.example.abonnements_service.scheduler;

import com.example.abonnements_service.event.EventType;
import com.example.abonnements_service.model.Abonnement;
import com.example.abonnements_service.model.Forfait;
import com.example.abonnements_service.model.StatutAbonnement;
import com.example.abonnements_service.repository.AbonnementRepository;
import com.example.abonnements_service.service.ForfaitService;
import com.example.abonnements_service.service.KafkaProducerService;
import com.example.abonnements_service.event.AbonnementEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled task to automatically expire subscriptions
 * Runs daily at 1:00 AM to check for expired subscriptions
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AbonnementExpirationScheduler {

    private final AbonnementRepository abonnementRepository;
    private final ForfaitService forfaitService;
    private final KafkaProducerService kafkaProducerService;

    /**
     * Check for expired subscriptions and update their status
     * Runs daily at 1:00 AM (cron: 0 0 1 * * ?)
     * Format: second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void checkExpiredSubscriptions() {
        log.info("Starting scheduled task: Check for expired subscriptions");

        LocalDate today = LocalDate.now();

        // Find all ACTIVE subscriptions where dateFin is before today
        List<Abonnement> expiredSubscriptions = abonnementRepository
                .findByStatutAndDateFinBefore(StatutAbonnement.ACTIVE, today);

        if (expiredSubscriptions.isEmpty()) {
            log.info("No expired subscriptions found");
            return;
        }

        log.info("Found {} expired subscriptions", expiredSubscriptions.size());

        // Update each expired subscription
        for (Abonnement abonnement : expiredSubscriptions) {
            try {
                // Update status to EXPIRED
                abonnement.setStatut(StatutAbonnement.EXPIRED);
                abonnementRepository.save(abonnement);

                log.info("Subscription {} expired for user {}", abonnement.getId(), abonnement.getUtilisateurId());

                // Publish expiration event to Kafka
                publishExpirationEvent(abonnement);

            } catch (Exception e) {
                log.error("Failed to process expired subscription {}: {}",
                        abonnement.getId(), e.getMessage(), e);
            }
        }

        log.info("Completed scheduled task: {} subscriptions expired", expiredSubscriptions.size());
    }

    /**
     * Publish expiration event to Kafka
     * Notifications service will consume this and send emails/SMS to users
     */
    private void publishExpirationEvent(Abonnement abonnement) {
        try {
            // Fetch forfait details for the event
            Forfait forfait = forfaitService.getForfaitEntityById(abonnement.getForfaitId());

            AbonnementEvent event = AbonnementEvent.builder()
                    .eventType(EventType.ABONNEMENT_EXPIRED.name())
                    .timestamp(LocalDateTime.now())
                    .abonnementId(abonnement.getId())
                    .utilisateurId(abonnement.getUtilisateurId())
                    .forfaitId(abonnement.getForfaitId())
                    .forfaitNom(forfait.getNom())
                    .dateDebut(abonnement.getDateDebut())
                    .dateFin(abonnement.getDateFin())
                    .prix(abonnement.getPrix())
                    .devise(abonnement.getDevise())
                    .statut(abonnement.getStatut())
                    .build();

            kafkaProducerService.publishAbonnementEvent(event);
            log.info("Published ABONNEMENT_EXPIRED event for user {}", abonnement.getUtilisateurId());

        } catch (Exception e) {
            log.error("Failed to publish expiration event for abonnement {}: {}",
                    abonnement.getId(), e.getMessage());
        }
    }
}
