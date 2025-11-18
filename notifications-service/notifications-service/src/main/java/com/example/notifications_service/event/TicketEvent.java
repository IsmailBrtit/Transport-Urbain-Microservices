package com.example.notifications_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement Kafka pour les billets
 * Publié par le service Billets sur le topic "ticket.events"
 *
 * STUB - À implémenter quand BC Billets sera créé
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketEvent {

    private String eventId;
    private EventType eventType;
    private LocalDateTime timestamp;
    private UUID ticketId;
    private UUID utilisateurId;
    private String utilisateurEmail;
    private String utilisateurNom;
    private String ticketType;
    private BigDecimal montant;
    private String devise;
    private LocalDateTime validJusque;
    private String qrCode;

    public enum EventType {
        TICKET_PURCHASED,
        TICKET_VALIDATED,
        TICKET_EXPIRED
    }
}
