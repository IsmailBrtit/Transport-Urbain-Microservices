package com.example.notifications_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Listener Kafka pour les événements utilisateur
 * Topic: user.events
 * <p>
 * STUB - À implémenter quand BC Utilisateurs sera créé
 * <p>
 * Événements attendus:
 * - USER_CREATED → "Bienvenue sur la plateforme"
 * - PASSWORD_RESET_REQUESTED → "Lien de réinitialisation"
 * - PASSWORD_CHANGED → "Confirmation de changement"
 * - ACCOUNT_ACTIVATED → "Votre compte est activé"
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    // TODO: Implémenter quand BC Utilisateurs sera créé
    // @KafkaListener(topics = "user.events", groupId = "notifications-service-group")
    // public void handleUserEvent(UserEvent event) {
    //     log.info("Événement utilisateur reçu: {}", event.getEventType());
    //
    //     switch (event.getEventType()) {
    //         case USER_CREATED -> handleUserCreated(event);
    //         case PASSWORD_RESET_REQUESTED -> handlePasswordReset(event);
    //         case PASSWORD_CHANGED -> handlePasswordChanged(event);
    //         case ACCOUNT_ACTIVATED -> handleAccountActivated(event);
    //     }
    // }

    // private void handleUserCreated(UserEvent event) {
    //     // Email de bienvenue
    // }

    // private void handlePasswordReset(UserEvent event) {
    //     // Email avec lien de réinitialisation
    // }

    // private void handlePasswordChanged(UserEvent event) {
    //     // Confirmation de changement de mot de passe
    // }

    // private void handleAccountActivated(UserEvent event) {
    //     // Confirmation d'activation du compte
    // }
}
