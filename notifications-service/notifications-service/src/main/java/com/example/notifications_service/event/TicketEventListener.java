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
public class TicketEventListener {

    private final EmailService emailService;
    private final NotificationService notificationService;
    private final UserServiceClient userServiceClient;

    @KafkaListener(
            topics = "ticket.events",
            groupId = "notifications-service-group",
            containerFactory = "ticketKafkaListenerContainerFactory"
    )
    public void handleTicketEvent(TicketEvent event) {
        log.info("Événement ticket reçu: {} pour ticket {} (eventId: {})",
                event.getEventType(), event.getTicketId(), event.getEventId());
        log.info("Détails ticket - Type: {}, Prix: {} {}, QR: {}",
                event.getType(), event.getPrix(), event.getDevise(),
                event.getQrCode() != null ? "present" : "null");

        try {
            enrichEventWithUserData(event);
            String eventType = event.getEventType();
            if ("TICKET_PURCHASED".equals(eventType)) {
                handleTicketPurchased(event);
            } else if ("TICKET_VALIDATED".equals(eventType)) {
                handleTicketValidated(event);
            } else if ("TICKET_EXPIRED".equals(eventType)) {
                handleTicketExpired(event);
            } else {
                log.warn("Type d'événement non géré: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'événement {}: {}",
                    event.getEventType(), e.getMessage(), e);
            saveFailedNotification(event, e.getMessage());
        }
    }

    private void enrichEventWithUserData(TicketEvent event) {
        if (event.getUtilisateurEmail() == null || event.getUtilisateurEmail().isBlank()) {
            log.info("Email absent dans l'événement ticket, récupération depuis User Service");

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

    private void handleTicketPurchased(TicketEvent event) {
        log.info("Traitement de TICKET_PURCHASED pour utilisateur {}", event.getUtilisateurId());

        Notification notification = notificationService.createNotification(
                event.getUtilisateurId(),
                "TICKET_PURCHASED",
                Canal.EMAIL,
                event.getUtilisateurEmail(),
                "Confirmation d'achat de billet - Urbain Transport",
                buildEmailContent(event, "Votre billet a été acheté avec succès"),
                event.getEventId()
        );

        try {
            emailService.sendTicketPurchasedEmail(event);
            notificationService.markAsSent(notification.getId());
        } catch (Exception e) {
            log.error("Échec d'envoi d'email pour TICKET_PURCHASED: {}", e.getMessage());
            notificationService.markAsFailed(notification.getId(), e.getMessage());
        }
    }

    private void handleTicketValidated(TicketEvent event) {
        log.info("Traitement de TICKET_VALIDATED pour utilisateur {}", event.getUtilisateurId());

        Notification notification = notificationService.createNotification(
                event.getUtilisateurId(),
                "TICKET_VALIDATED",
                Canal.EMAIL,
                event.getUtilisateurEmail(),
                "Validation de billet - Urbain Transport",
                buildEmailContent(event, "Votre billet a été validé"),
                event.getEventId()
        );

        try {
            emailService.sendTicketValidatedEmail(event);
            notificationService.markAsSent(notification.getId());
        } catch (Exception e) {
            log.error("Échec d'envoi d'email pour TICKET_VALIDATED: {}", e.getMessage());
            notificationService.markAsFailed(notification.getId(), e.getMessage());
        }
    }

    private void handleTicketExpired(TicketEvent event) {
        log.info("Traitement de TICKET_EXPIRED pour utilisateur {}", event.getUtilisateurId());

        Notification notification = notificationService.createNotification(
                event.getUtilisateurId(),
                "TICKET_EXPIRED",
                Canal.EMAIL,
                event.getUtilisateurEmail(),
                "Billet expiré - Urbain Transport",
                buildEmailContent(event, "Votre billet a expiré"),
                event.getEventId()
        );

        try {
            emailService.sendTicketExpiredEmail(event);
            notificationService.markAsSent(notification.getId());
        } catch (Exception e) {
            log.error("Échec d'envoi d'email pour TICKET_EXPIRED: {}", e.getMessage());
            notificationService.markAsFailed(notification.getId(), e.getMessage());
        }
    }

    private void saveFailedNotification(TicketEvent event, String errorMessage) {
        try {
            notificationService.createNotification(
                    event.getUtilisateurId(),
                    event.getEventType() != null ? event.getEventType() : "UNKNOWN",
                    Canal.EMAIL,
                    event.getUtilisateurEmail(),
                    "Erreur notification ticket",
                    "Échec de traitement: " + errorMessage,
                    event.getEventId()
            );
        } catch (Exception e) {
            log.error("Impossible de sauvegarder la notification échouée: {}", e.getMessage());
        }
    }

    private String buildEmailContent(TicketEvent event, String message) {
        return String.format("%s - Type: %s, Prix: %s %s, Valide jusqu'à: %s",
                message,
                event.getType(),
                event.getPrix(),
                event.getDevise(),
                event.getValidJusque()
        );
    }
}
