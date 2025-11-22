package com.example.abonnements_service.scheduler;

import com.example.abonnements_service.client.UserServiceClient;
import com.example.abonnements_service.dto.UserDto;
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
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpirationScheduler {

    private final AbonnementRepository abonnementRepository;
    private final ForfaitService forfaitService;
    private final UserServiceClient userServiceClient;
    private final KafkaProducerService kafkaProducerService;

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void checkExpiredAbonnements() {
        log.info("Démarrage de la vérification des abonnements expirés...");

        LocalDate today = LocalDate.now();

        List<Abonnement> expiredAbonnements = abonnementRepository
                .findByStatutAndDateFinBefore(StatutAbonnement.ACTIVE, today);

        if (expiredAbonnements.isEmpty()) {
            log.info("Aucun abonnement expiré trouvé");
            return;
        }

        log.info("{} abonnement(s) expiré(s) trouvé(s)", expiredAbonnements.size());

        int successCount = 0;
        int failCount = 0;

        for (Abonnement abonnement : expiredAbonnements) {
            try {
                abonnement.setStatut(StatutAbonnement.EXPIRED);
                abonnementRepository.save(abonnement);

                UserDto user = userServiceClient.getUserById(abonnement.getUtilisateurId());
                if (user == null) {
                    log.warn("Utilisateur non trouvé pour abonnement expiré: {}. Événement publié quand même avec email par défaut.",
                            abonnement.getUtilisateurId());
                }

                Forfait forfait = forfaitService.getForfaitEntityById(abonnement.getForfaitId());

                publishExpirationEvent(abonnement, forfait, user);

                successCount++;
                log.info("Abonnement {} marqué comme expiré et événement publié", abonnement.getId());

            } catch (Exception e) {
                failCount++;
                log.error("Erreur lors du traitement de l'abonnement expiré {}: {}", abonnement.getId(), e.getMessage());
            }
        }

        log.info("Vérification terminée: {} succès, {} échecs", successCount, failCount);
    }

    private void publishExpirationEvent(Abonnement abonnement, Forfait forfait, UserDto user) {
        try {
            String eventId = UUID.randomUUID().toString();

            AbonnementEvent event = AbonnementEvent.builder()
                    .eventId(eventId)
                    .eventType(EventType.ABONNEMENT_EXPIRED.name())
                    .timestamp(LocalDateTime.now())
                    .abonnementId(abonnement.getId())
                    .utilisateurId(abonnement.getUtilisateurId())
                    .utilisateurEmail(user != null ? user.getEmail() : "unknown@example.com")
                    .utilisateurNom(user != null ? user.getFullName() : "Utilisateur Inconnu")
                    .forfaitId(abonnement.getForfaitId())
                    .forfaitNom(forfait.getNom())
                    .dateDebut(abonnement.getDateDebut())
                    .dateFin(abonnement.getDateFin())
                    .prix(abonnement.getPrix())
                    .devise(abonnement.getDevise())
                    .statut(abonnement.getStatut())
                    .build();

            kafkaProducerService.publishAbonnementEvent(event);
            log.info("Événement ABONNEMENT_EXPIRED publié (eventId: {})", eventId);

        } catch (Exception e) {
            log.error("Échec de publication de l'événement d'expiration: {}", e.getMessage());
        }
    }
}
