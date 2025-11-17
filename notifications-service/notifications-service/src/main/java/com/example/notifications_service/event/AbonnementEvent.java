package com.example.notifications_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement Kafka pour les abonnements
 * Publié par le service Abonnements sur le topic "abonnement.events"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbonnementEvent {

    /**
     * ID unique de l'événement (pour idempotence)
     */
    private String eventId;

    /**
     * Type d'événement
     */
    private EventType eventType;

    /**
     * Timestamp de l'événement
     */
    private LocalDateTime timestamp;

    /**
     * ID de l'abonnement concerné
     */
    private UUID abonnementId;

    /**
     * ID de l'utilisateur
     */
    private UUID utilisateurId;

    /**
     * Email de l'utilisateur (pour notifications)
     */
    private String utilisateurEmail;

    /**
     * Nom de l'utilisateur
     */
    private String utilisateurNom;

    /**
     * ID du forfait
     */
    private UUID forfaitId;

    /**
     * Nom du forfait
     */
    private String forfaitNom;

    /**
     * Date de début
     */
    private LocalDate dateDebut;

    /**
     * Date de fin
     */
    private LocalDate dateFin;

    /**
     * Prix de l'abonnement
     */
    private BigDecimal prix;

    /**
     * Devise
     */
    private String devise;

    /**
     * Numéro de facture (si applicable)
     */
    private String numeroFacture;

    /**
     * Statut de l'abonnement
     */
    private String statut;

    /**
     * Enum pour les types d'événements d'abonnement
     */
    public enum EventType {
        ABONNEMENT_CREATED,
        ABONNEMENT_RENEWED,
        ABONNEMENT_CANCELED,
        ABONNEMENT_EXPIRED
    }
}
