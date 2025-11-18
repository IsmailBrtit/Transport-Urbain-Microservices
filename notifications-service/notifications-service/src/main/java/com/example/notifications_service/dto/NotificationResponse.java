package com.example.notifications_service.dto;

import com.example.notifications_service.model.Canal;
import com.example.notifications_service.model.StatutNotification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO pour les réponses des notifications
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private UUID id;
    private UUID utilisateurId;
    private String type;
    private Canal canal;
    private String destinataire;
    private String sujet;
    private String contenu;
    private StatutNotification statut;
    private LocalDateTime envoyeLe;
    private String erreurMessage;
    private LocalDateTime createLe;
}
