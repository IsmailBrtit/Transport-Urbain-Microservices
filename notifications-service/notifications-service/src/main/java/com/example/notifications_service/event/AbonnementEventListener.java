package com.example.notifications_service.event;

import com.example.notifications_service.model.Canal;
import com.example.notifications_service.model.Notification;
import com.example.notifications_service.service.EmailService;
import com.example.notifications_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listener Kafka pour les événements d'abonnement
 * Topic: abonnement.events
 * <p>
 * Responsabilité:
 * - Écouter les événements Kafka du service Abonnements
 * - Envoyer des emails de notification
 * - Sauvegarder l'historique des notifications dans la base de données
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AbonnementEventListener {

    private final EmailService emailService;
    private final NotificationService notificationService;

    /**
     * Écouter tous les événements d'abonnement
     * <p>
     * Kafka Consumer Group: notifications-service-group
     * Topic: abonnement.events
     */
    @KafkaListener(
            topics = "abonnement.events",
            groupId = "notifications-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAbonnementEvent(AbonnementEvent event) {
        log.info("Événement reçu: {} pour abonnement {} (eventId: {})",
                event.getEventType(), event.getAbonnementId(), event.getEventId());

        try {
            // Router vers le bon handler selon le type d'événement
            switch (event.getEventType()) {
                case ABONNEMENT_CREATED -> handleAbonnementCreated(event);
                case ABONNEMENT_RENEWED -> handleAbonnementRenewed(event);
                case ABONNEMENT_CANCELED -> handleAbonnementCanceled(event);
                case ABONNEMENT_EXPIRED -> handleAbonnementExpired(event);
                default -> log.warn("Type d'événement non géré: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'événement {}: {}",
                    event.getEventType(), e.getMessage(), e);

            // Sauvegarder la notification avec statut ECHEC
            saveFailedNotification(event, e.getMessage());
        }
    }

    /**
     * Gérer l'événement ABONNEMENT_CREATED
     */
    private void handleAbonnementCreated(AbonnementEvent event) {
        log.info("Traitement de ABONNEMENT_CREATED pour utilisateur {}", event.getUtilisateurId());

        // Créer l'enregistrement de notification
        Notification notification = notificationService.createNotification(
                event.getUtilisateurId(),
                "ABONNEMENT_CREATED",
                Canal.EMAIL,
                event.getUtilisateurEmail(),
                "Confirmation de votre abonnement Urbain",
                buildEmailContent(event, "Votre abonnement a été créé avec succès"),
                event.getEventId()
        );

        try {
            // Envoyer l'email
            emailService.sendAbonnementCreatedEmail(event);

            // Marquer comme envoyé
            notificationService.markAsSent(notification.getId());

        } catch (Exception e) {
            log.error("Échec d'envoi d'email pour ABONNEMENT_CREATED: {}", e.getMessage());
            notificationService.markAsFailed(notification.getId(), e.getMessage());
        }
    }

    /**
     * Gérer l'événement ABONNEMENT_RENEWED
     */
    private void handleAbonnementRenewed(AbonnementEvent event) {
        log.info("Traitement de ABONNEMENT_RENEWED pour utilisateur {}", event.getUtilisateurId());

        Notification notification = notificationService.createNotification(
                event.getUtilisateurId(),
                "ABONNEMENT_RENEWED",
                Canal.EMAIL,
                event.getUtilisateurEmail(),
                "Renouvellement de votre abonnement Urbain",
                buildEmailContent(event, "Votre abonnement a été renouvelé"),
                event.getEventId()
        );

        try {
            emailService.sendAbonnementRenewedEmail(event);
            notificationService.markAsSent(notification.getId());
        } catch (Exception e) {
            log.error("Échec d'envoi d'email pour ABONNEMENT_RENEWED: {}", e.getMessage());
            notificationService.markAsFailed(notification.getId(), e.getMessage());
        }
    }

    /**
     * Gérer l'événement ABONNEMENT_CANCELED
     */
    private void handleAbonnementCanceled(AbonnementEvent event) {
        log.info("Traitement de ABONNEMENT_CANCELED pour utilisateur {}", event.getUtilisateurId());

        Notification notification = notificationService.createNotification(
                event.getUtilisateurId(),
                "ABONNEMENT_CANCELED",
                Canal.EMAIL,
                event.getUtilisateurEmail(),
                "Annulation de votre abonnement Urbain",
                buildEmailContent(event, "Votre abonnement a été annulé"),
                event.getEventId()
        );

        try {
            emailService.sendAbonnementCanceledEmail(event);
            notificationService.markAsSent(notification.getId());
        } catch (Exception e) {
            log.error("Échec d'envoi d'email pour ABONNEMENT_CANCELED: {}", e.getMessage());
            notificationService.markAsFailed(notification.getId(), e.getMessage());
        }
    }

    /**
     * Gérer l'événement ABONNEMENT_EXPIRED
     */
    private void handleAbonnementExpired(AbonnementEvent event) {
        log.info("Traitement de ABONNEMENT_EXPIRED pour utilisateur {}", event.getUtilisateurId());

        Notification notification = notificationService.createNotification(
                event.getUtilisateurId(),
                "ABONNEMENT_EXPIRED",
                Canal.EMAIL,
                event.getUtilisateurEmail(),
                "Votre abonnement Urbain a expiré",
                buildEmailContent(event, "Votre abonnement a expiré"),
                event.getEventId()
        );

        try {
            emailService.sendAbonnementExpiredEmail(event);
            notificationService.markAsSent(notification.getId());
        } catch (Exception e) {
            log.error("Échec d'envoi d'email pour ABONNEMENT_EXPIRED: {}", e.getMessage());
            notificationService.markAsFailed(notification.getId(), e.getMessage());
        }
    }

    /**
     * Sauvegarder une notification échouée
     */
    private void saveFailedNotification(AbonnementEvent event, String errorMessage) {
        try {
            notificationService.createNotification(
                    event.getUtilisateurId(),
                    event.getEventType().name(),
                    Canal.EMAIL,
                    event.getUtilisateurEmail(),
                    "Erreur notification",
                    "Échec de traitement: " + errorMessage,
                    event.getEventId()
            );
        } catch (Exception e) {
            log.error("Impossible de sauvegarder la notification échouée: {}", e.getMessage());
        }
    }

    /**
     * Construire le contenu de l'email (pour l'historique)
     */
    private String buildEmailContent(AbonnementEvent event, String message) {
        return String.format("%s - %s (%s à %s)",
                message,
                event.getForfaitNom(),
                event.getDateDebut(),
                event.getDateFin()
        );
    }
}
