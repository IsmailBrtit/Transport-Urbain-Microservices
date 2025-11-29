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

    private String eventType;  // CREATED, RENEWED, CANCELED, EXPIRED
    private LocalDateTime timestamp;
    private UUID abonnementId;
    private UUID utilisateurId;
    private UUID forfaitId;
    private String forfaitNom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private BigDecimal prix;
    private Devise devise;
    private StatutAbonnement statut;
    private String numeroFacture;  // Associated invoice number

    // For analytics
    private String metadata;
}
