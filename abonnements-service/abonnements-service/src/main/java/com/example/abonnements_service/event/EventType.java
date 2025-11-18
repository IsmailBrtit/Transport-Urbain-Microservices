package com.example.abonnements_service.event;

/**
 * Event types for subscription lifecycle
 */
public enum EventType {
    ABONNEMENT_CREATED,     // New subscription created
    ABONNEMENT_RENEWED,     // Subscription renewed
    ABONNEMENT_CANCELED,    // Subscription canceled by user/admin
    ABONNEMENT_EXPIRED      // Subscription expired (automatic)
}
