package com.example.notifications_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;

    private UUID ticketId;
    private UUID utilisateurId;

    private String utilisateurEmail;
    private String utilisateurNom;

    private String type;
    private BigDecimal prix;
    private String devise;
    private String qrCode;
    private LocalDateTime validJusque;

    public boolean isTicketPurchased() {
        return "TICKET_PURCHASED".equals(eventType);
    }

    public boolean isTicketValidated() {
        return "TICKET_VALIDATED".equals(eventType);
    }

    public boolean isTicketExpired() {
        return "TICKET_EXPIRED".equals(eventType);
    }
}
