package com.example.notifications_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité Notification - Enregistrement de toutes les notifications envoyées
 *
 * Responsabilité: Tracer l'historique des notifications envoyées aux utilisateurs
 * - Stocke les détails de chaque notification (type, destinataire, contenu, statut)
 * - Permet l'audit et le suivi des communications système
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * ID de l'utilisateur destinataire (référence vers BC Utilisateurs)
     * Pas de relation JPA - microservices indépendants
     */
    @Column(nullable = false)
    private UUID utilisateurId;

    /**
     * Type de notification basé sur l'événement source
     * Exemples: "ABONNEMENT_CREATED", "TICKET_PURCHASED", "DISRUPTION_REPORTED"
     */
    @Column(nullable = false, length = 100)
    private String type;

    /**
     * Canal de notification (EMAIL, SMS, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Canal canal;

    /**
     * Adresse email ou numéro de téléphone du destinataire
     */
    @Column(nullable = false, length = 255)
    private String destinataire;

    /**
     * Sujet de l'email (ou titre pour SMS/Push)
     */
    @Column(nullable = false, length = 255)
    private String sujet;

    /**
     * Contenu du message (HTML pour email, texte pour SMS)
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    /**
     * Statut de l'envoi (EN_ATTENTE, ENVOYE, ECHEC)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutNotification statut;

    /**
     * Date et heure d'envoi (ou de tentative d'envoi)
     */
    @Column(name = "envoye_le")
    private LocalDateTime envoyeLe;

    /**
     * Message d'erreur en cas d'échec
     * Null si envoi réussi
     */
    @Column(name = "erreur_message", columnDefinition = "TEXT")
    private String erreurMessage;

    /**
     * Date de création de l'enregistrement
     */
    @Column(name = "create_le", nullable = false, updatable = false)
    private LocalDateTime createLe;

    /**
     * ID de l'événement Kafka source (pour traçabilité et idempotence)
     * Permet d'éviter les doublons si un événement est reçu plusieurs fois
     */
    @Column(name = "event_id", unique = true)
    private String eventId;

    /**
     * Lifecycle callback - Set creation timestamp
     */
    @PrePersist
    protected void onCreate() {
        this.createLe = LocalDateTime.now();
        if (this.statut == null) {
            this.statut = StatutNotification.EN_ATTENTE;
        }
    }

    /**
     * Marquer la notification comme envoyée avec succès
     */
    public void marquerEnvoye() {
        this.statut = StatutNotification.ENVOYE;
        this.envoyeLe = LocalDateTime.now();
        this.erreurMessage = null;
    }

    /**
     * Marquer la notification comme échouée
     */
    public void marquerEchec(String messageErreur) {
        this.statut = StatutNotification.ECHEC;
        this.envoyeLe = LocalDateTime.now();
        this.erreurMessage = messageErreur;
    }
}
