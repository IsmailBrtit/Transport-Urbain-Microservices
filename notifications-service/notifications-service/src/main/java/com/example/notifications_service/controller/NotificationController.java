package com.example.notifications_service.controller;

import com.example.notifications_service.dto.NotificationResponse;
import com.example.notifications_service.dto.TestEmailRequest;
import com.example.notifications_service.model.StatutNotification;
import com.example.notifications_service.service.EmailService;
import com.example.notifications_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Contrôleur REST pour la gestion des notifications
 * Base path: /api/v1/notifications
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "API de gestion des notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final EmailService emailService;

    /**
     * Récupérer toutes les notifications
     */
    @GetMapping
    @Operation(summary = "Liste toutes les notifications")
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        log.info("GET /api/v1/notifications - Récupération de toutes les notifications");
        List<NotificationResponse> notifications = notificationService.findAll();
        return ResponseEntity.ok(notifications);
    }

    /**
     * Récupérer une notification par ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupère une notification par son ID")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable UUID id) {
        log.info("GET /api/v1/notifications/{} - Récupération de la notification", id);
        NotificationResponse notification = notificationService.findById(id);
        return ResponseEntity.ok(notification);
    }

    /**
     * Récupérer toutes les notifications d'un utilisateur
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Récupère toutes les notifications d'un utilisateur")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUserId(@PathVariable UUID userId) {
        log.info("GET /api/v1/notifications/user/{} - Récupération des notifications de l'utilisateur", userId);
        List<NotificationResponse> notifications = notificationService.findByUtilisateurId(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Récupérer les notifications par type
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Récupère les notifications par type")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByType(@PathVariable String type) {
        log.info("GET /api/v1/notifications/type/{} - Récupération des notifications par type", type);
        List<NotificationResponse> notifications = notificationService.findByType(type);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Récupérer les notifications par statut
     */
    @GetMapping("/statut/{statut}")
    @Operation(summary = "Récupère les notifications par statut")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByStatut(@PathVariable StatutNotification statut) {
        log.info("GET /api/v1/notifications/statut/{} - Récupération des notifications par statut", statut);
        List<NotificationResponse> notifications = notificationService.findByStatut(statut);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Statistiques des notifications
     */
    @GetMapping("/stats")
    @Operation(summary = "Récupère les statistiques des notifications")
    public ResponseEntity<Map<String, Long>> getNotificationStats() {
        log.info("GET /api/v1/notifications/stats - Récupération des statistiques");

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", notificationService.findAll().size() * 1L);
        stats.put("enAttente", notificationService.countByStatut(StatutNotification.EN_ATTENTE));
        stats.put("envoye", notificationService.countByStatut(StatutNotification.ENVOYE));
        stats.put("echec", notificationService.countByStatut(StatutNotification.ECHEC));

        return ResponseEntity.ok(stats);
    }

    /**
     * Envoyer un email de test
     */
    @PostMapping("/test")
    @Operation(summary = "Envoie un email de test")
    public ResponseEntity<Map<String, String>> sendTestEmail(@Valid @RequestBody TestEmailRequest request) {
        log.info("POST /api/v1/notifications/test - Envoi d'email de test à {}", request.getDestinataire());

        try {
            emailService.sendTestEmail(
                    request.getDestinataire(),
                    request.getNom(),
                    request.getMessage()
            );

            Map<String, String> response = new HashMap<>();
            response.put("message", "Email de test envoyé avec succès");
            response.put("destinataire", request.getDestinataire());

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de test: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Échec d'envoi de l'email");
            errorResponse.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
