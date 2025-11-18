package com.example.notifications_service.model;

/**
 * Statut d'une notification
 */
public enum StatutNotification {
    EN_ATTENTE("En attente d'envoi"),
    ENVOYE("Envoyée avec succès"),
    ECHEC("Échec d'envoi");

    private final String description;

    StatutNotification(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
