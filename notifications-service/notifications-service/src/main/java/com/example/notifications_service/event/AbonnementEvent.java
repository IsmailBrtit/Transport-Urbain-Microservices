package com.example.notifications_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbonnementEvent {

    private String eventId;

    private EventType eventType;

    private LocalDateTime timestamp;

    private UUID abonnementId;

    private UUID utilisateurId;

    private String utilisateurEmail;

    private String utilisateurNom;

    private UUID forfaitId;

    private String forfaitNom;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    private BigDecimal prix;

    private String devise;

    private String numeroFacture;

    private String statut;

    public enum EventType {
        ABONNEMENT_CREATED,
        ABONNEMENT_RENEWED,
        ABONNEMENT_CANCELED,
        ABONNEMENT_EXPIRED
    }
}
