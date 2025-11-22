package com.example.billets_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement Kafka pour les tickets
 * Topic: ticket.events
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketEvent {

    private String eventId;  // UUID unique pour idempotence
    private String eventType;  // TICKET_PURCHASED, TICKET_VALIDATED, TICKET_EXPIRED
    private LocalDateTime timestamp;

    // Ticket data
    private UUID ticketId;
    private UUID utilisateurId;

    // ✅ Event-Carried State Transfer: User data
    private String utilisateurEmail;
    private String utilisateurNom;

    // Ticket details
    private String type;  // SIMPLE, ALLER_RETOUR, JOURNALIER
    private BigDecimal prix;
    private String devise;
    private String qrCode;
    private LocalDateTime validJusque;
}
