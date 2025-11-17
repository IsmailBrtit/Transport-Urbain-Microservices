package com.example.notifications_service.model;

/**
 * Canal de notification (Notification Channel)
 * Pour Phase 1: EMAIL uniquement
 * Future: SMS, PUSH, etc.
 */
public enum Canal {
    EMAIL("Email"),
    SMS("SMS");  // Future implementation

    private final String nom;

    Canal(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }
}
