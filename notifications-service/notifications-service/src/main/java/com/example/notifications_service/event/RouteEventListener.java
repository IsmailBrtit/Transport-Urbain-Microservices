package com.example.notifications_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Listener Kafka pour les événements de trajets
 * Topic: route.events
 * <p>
 * STUB - À implémenter quand BC Trajets sera créé
 * <p>
 * Événements attendus:
 * - DISRUPTION_REPORTED → "Perturbation sur votre ligne"
 * - ROUTE_UPDATED → "Modification d'itinéraire"
 * - SCHEDULE_CHANGED → "Changement d'horaires"
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RouteEventListener {

    // TODO: Implémenter quand BC Trajets sera créé
    // @KafkaListener(topics = "route.events", groupId = "notifications-service-group")
    // public void handleRouteEvent(RouteEvent event) {
    //     log.info("Événement trajet reçu: {}", event.getEventType());
    //
    //     switch (event.getEventType()) {
    //         case DISRUPTION_REPORTED -> handleDisruptionReported(event);
    //         case ROUTE_UPDATED -> handleRouteUpdated(event);
    //         case SCHEDULE_CHANGED -> handleScheduleChanged(event);
    //     }
    // }

    // private void handleDisruptionReported(RouteEvent event) {
    //     // Envoyer alerte de perturbation
    //     // Cibler les utilisateurs abonnés à cette ligne
    // }

    // private void handleRouteUpdated(RouteEvent event) {
    //     // Notification de modification d'itinéraire
    // }

    // private void handleScheduleChanged(RouteEvent event) {
    //     // Notification de changement d'horaires
    // }
}
