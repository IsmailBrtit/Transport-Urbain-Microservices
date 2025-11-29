package com.example.notifications_service.service;

import com.example.notifications_service.dto.NotificationResponse;
import com.example.notifications_service.exception.ResourceNotFoundException;
import com.example.notifications_service.model.Canal;
import com.example.notifications_service.model.Notification;
import com.example.notifications_service.model.StatutNotification;
import com.example.notifications_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service de gestion des notifications
 * Responsabilité: CRUD et business logic pour les notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Créer et sauvegarder une notification
     */
    @Transactional
    public Notification createNotification(
            UUID utilisateurId,
            String type,
            Canal canal,
            String destinataire,
            String sujet,
            String contenu,
            String eventId
    ) {
        // Vérifier si une notification existe déjà pour cet event (idempotence)
        if (eventId != null && notificationRepository.existsByEventId(eventId)) {
            log.warn("Notification déjà existante pour eventId: {}. Ignoré pour éviter les doublons.", eventId);
            return notificationRepository.findByEventId(eventId).orElseThrow();
        }

        Notification notification = Notification.builder()
                .utilisateurId(utilisateurId)
                .type(type)
                .canal(canal)
                .destinataire(destinataire)
                .sujet(sujet)
                .contenu(contenu)
                .statut(StatutNotification.EN_ATTENTE)
                .eventId(eventId)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification créée avec ID: {} pour utilisateur: {}", saved.getId(), utilisateurId);

        return saved;
    }

    /**
     * Marquer une notification comme envoyée
     */
    @Transactional
    public void markAsSent(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée avec ID: " + notificationId));

        notification.marquerEnvoye();
        notificationRepository.save(notification);

        log.info("Notification {} marquée comme envoyée", notificationId);
    }

    /**
     * Marquer une notification comme échouée
     */
    @Transactional
    public void markAsFailed(UUID notificationId, String errorMessage) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée avec ID: " + notificationId));

        notification.marquerEchec(errorMessage);
        notificationRepository.save(notification);

        log.error("Notification {} marquée comme échouée: {}", notificationId, errorMessage);
    }

    /**
     * Récupérer toutes les notifications
     */
    public List<NotificationResponse> findAll() {
        log.debug("Récupération de toutes les notifications");
        return notificationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer une notification par ID
     */
    public NotificationResponse findById(UUID id) {
        log.debug("Récupération de la notification avec ID: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée avec ID: " + id));
        return mapToResponse(notification);
    }

    /**
     * Récupérer toutes les notifications d'un utilisateur
     */
    public List<NotificationResponse> findByUtilisateurId(UUID utilisateurId) {
        log.debug("Récupération des notifications pour utilisateur: {}", utilisateurId);
        return notificationRepository.findByUtilisateurIdOrderByCreateLeDesc(utilisateurId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les notifications par type
     */
    public List<NotificationResponse> findByType(String type) {
        log.debug("Récupération des notifications de type: {}", type);
        return notificationRepository.findByType(type).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les notifications par statut
     */
    public List<NotificationResponse> findByStatut(StatutNotification statut) {
        log.debug("Récupération des notifications avec statut: {}", statut);
        return notificationRepository.findByStatut(statut).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Compter les notifications par statut
     */
    public long countByStatut(StatutNotification statut) {
        return notificationRepository.countByStatut(statut);
    }

    /**
     * Mapper une entité Notification vers NotificationResponse DTO
     */
    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .utilisateurId(notification.getUtilisateurId())
                .type(notification.getType())
                .canal(notification.getCanal())
                .destinataire(notification.getDestinataire())
                .sujet(notification.getSujet())
                .contenu(notification.getContenu())
                .statut(notification.getStatut())
                .envoyeLe(notification.getEnvoyeLe())
                .erreurMessage(notification.getErreurMessage())
                .createLe(notification.getCreateLe())
                .build();
    }
}
