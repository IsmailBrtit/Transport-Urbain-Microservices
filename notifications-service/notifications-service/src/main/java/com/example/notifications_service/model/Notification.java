package com.example.notifications_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

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

    @Column(nullable = false)
    private UUID utilisateurId;

    @Column(nullable = false, length = 100)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Canal canal;

    @Column(nullable = false, length = 255)
    private String destinataire;

    @Column(nullable = false, length = 255)
    private String sujet;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutNotification statut;

    @Column(name = "envoye_le")
    private LocalDateTime envoyeLe;

    @Column(name = "erreur_message", columnDefinition = "TEXT")
    private String erreurMessage;

    @Column(name = "create_le", nullable = false, updatable = false)
    private LocalDateTime createLe;

    @Column(name = "event_id", unique = true)
    private String eventId;

    @PrePersist
    protected void onCreate() {
        this.createLe = LocalDateTime.now();
        if (this.statut == null) {
            this.statut = StatutNotification.EN_ATTENTE;
        }
    }

    public void marquerEnvoye() {
        this.statut = StatutNotification.ENVOYE;
        this.envoyeLe = LocalDateTime.now();
        this.erreurMessage = null;
    }

    public void marquerEchec(String messageErreur) {
        this.statut = StatutNotification.ECHEC;
        this.envoyeLe = LocalDateTime.now();
        this.erreurMessage = messageErreur;
    }
}
