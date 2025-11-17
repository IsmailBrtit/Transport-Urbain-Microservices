package com.example.notifications_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement Kafka pour les trajets et perturbations
 * Publié par le service Trajets sur le topic "route.events"
 *
 * STUB - À implémenter quand BC Trajets sera créé
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteEvent {

    private String eventId;
    private EventType eventType;
    private LocalDateTime timestamp;
    private UUID ligneId;
    private String ligneNumero;
    private String ligneNom;
    private String titre;
    private String description;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String severite;

    public enum EventType {
        DISRUPTION_REPORTED,
        ROUTE_UPDATED,
        SCHEDULE_CHANGED
    }
}
