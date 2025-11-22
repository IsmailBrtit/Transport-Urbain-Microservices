package com.example.notifications_service.event;

import com.example.notifications_service.client.UserServiceClient;
import com.example.notifications_service.dto.UserDto;
import com.example.notifications_service.model.Canal;
import com.example.notifications_service.model.Notification;
import com.example.notifications_service.service.EmailService;
import com.example.notifications_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AbonnementEventListener {

    private final EmailService emailService;
    private final NotificationService notificationService;
    private final UserServiceClient userServiceClient;

    @KafkaListener(
            topics = "abonnement.events",
            groupId = "notifications-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAbonnementEvent(AbonnementEvent event) {
        log.info("Événement reçu: {} pour abonnement {} (eventId: {})",
                event.getEventType(), event.getAbonnementId(), event.getEventId());

        try {
            enrichEventWithUserData(event);

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

            saveFailedNotification(event, e.getMessage());
        }
    }

    private void enrichEventWithUserData(AbonnementEvent event) {
        if (event.getUtilisateurEmail() == null || event.getUtilisateurEmail().isBlank()) {
            log.info("Email absent dans l'événement, récupération depuis User Service pour utilisateur {}",
                    event.getUtilisateurId());

            try {
                UserDto user = userServiceClient.getUserById(event.getUtilisateurId());
                event.setUtilisateurEmail(user.getEmail());
                event.setUtilisateurNom(user.getFullName() != null ? user.getFullName() :
                        user.getFirstName() + " " + user.getLastName());

                log.info("Données utilisateur enrichies: {} ({})",
                        event.getUtilisateurNom(), event.getUtilisateurEmail());
            } catch (Exception e) {
                log.error("Impossible de récupérer les données utilisateur: {}", e.getMessage());
                throw new RuntimeException("Failed to enrich event with user data: " + e.getMessage(), e);
            }
        }
    }

    private void handleAbonnementCreated(AbonnementEvent event) {
        log.info("Traitement de ABONNEMENT_CREATED pour utilisateur {}", event.getUtilisateurId());

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
            emailService.sendAbonnementCreatedEmail(event);
            notificationService.markAsSent(notification.getId());
        } catch (Exception e) {
            log.error("Échec d'envoi d'email pour ABONNEMENT_CREATED: {}", e.getMessage());
            notificationService.markAsFailed(notification.getId(), e.getMessage());
        }
    }

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

    private String buildEmailContent(AbonnementEvent event, String message) {
        return String.format("%s - %s (%s à %s)",
                message,
                event.getForfaitNom(),
                event.getDateDebut(),
                event.getDateFin()
        );
    }
}
