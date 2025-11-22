package com.example.abonnements_service.event;

import com.example.abonnements_service.model.Devise;
import com.example.abonnements_service.model.StatutAbonnement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base event for all subscription-related events
 * Published to Kafka for inter-service communication
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbonnementEvent {

    // Event metadata
    private String eventId;  // Unique event ID for idempotence (UUID)
    private String eventType;  // CREATED, RENEWED, CANCELED, EXPIRED
    private LocalDateTime timestamp;

    // Abonnement data
    private UUID abonnementId;
    private UUID utilisateurId;

    // ✅ Event-Carried State Transfer: User data (from User Service)
    private String utilisateurEmail;  // Email for notifications
    private String utilisateurNom;    // Full name for personalization

    // Forfait data
    private UUID forfaitId;
    private String forfaitNom;

    // Subscription dates
    private LocalDate dateDebut;
    private LocalDate dateFin;

    // Price snapshot
    private BigDecimal prix;
    private Devise devise;

    // Status
    private StatutAbonnement statut;

    // Invoice
    private String numeroFacture;  // Associated invoice number

    // For analytics
    private String metadata;
}
