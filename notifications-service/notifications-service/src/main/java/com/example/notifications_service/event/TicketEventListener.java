package com.example.notifications_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Listener Kafka pour les événements de billets
 * Topic: ticket.events
 * <p>
 * STUB - À implémenter quand BC Billets sera créé
 * <p>
 * Événements attendus:
 * - TICKET_PURCHASED → "Votre billet a été acheté"
 * - TICKET_VALIDATED → "Votre billet a été validé"
 * - TICKET_EXPIRED → "Votre billet a expiré"
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TicketEventListener {

    // TODO: Implémenter quand BC Billets sera créé
    // @KafkaListener(topics = "ticket.events", groupId = "notifications-service-group")
    // public void handleTicketEvent(TicketEvent event) {
    //     log.info("Événement ticket reçu: {}", event.getEventType());
    //
    //     switch (event.getEventType()) {
    //         case TICKET_PURCHASED -> handleTicketPurchased(event);
    //         case TICKET_VALIDATED -> handleTicketValidated(event);
    //         case TICKET_EXPIRED -> handleTicketExpired(event);
    //     }
    // }

    // private void handleTicketPurchased(TicketEvent event) {
    //     // Envoyer email avec QR code
    // }

    // private void handleTicketValidated(TicketEvent event) {
    //     // Confirmation de validation
    // }

    // private void handleTicketExpired(TicketEvent event) {
    //     // Notification d'expiration
    // }
}
