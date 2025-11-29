package com.example.notifications_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement Kafka pour les utilisateurs
 * Publié par le service Utilisateurs sur le topic "user.events"
 *
 * STUB - À implémenter quand BC Utilisateurs sera créé
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEvent {

    private String eventId;
    private EventType eventType;
    private LocalDateTime timestamp;
    private UUID utilisateurId;
    private String email;
    private String nomComplet;
    private String resetToken;
    private LocalDateTime resetTokenExpiration;

    public enum EventType {
        USER_CREATED,
        PASSWORD_RESET_REQUESTED,
        PASSWORD_CHANGED,
        ACCOUNT_ACTIVATED
    }
}
