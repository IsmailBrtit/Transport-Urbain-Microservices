package com.example.notifications_service.repository;

import com.example.notifications_service.model.Canal;
import com.example.notifications_service.model.Notification;
import com.example.notifications_service.model.StatutNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Notification
 * Gère la persistance et les requêtes personnalisées
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Trouver toutes les notifications d'un utilisateur
     */
    List<Notification> findByUtilisateurId(UUID utilisateurId);

    /**
     * Trouver toutes les notifications d'un utilisateur triées par date
     */
    List<Notification> findByUtilisateurIdOrderByCreateLeDesc(UUID utilisateurId);

    /**
     * Trouver les notifications par type
     */
    List<Notification> findByType(String type);

    /**
     * Trouver les notifications par statut
     */
    List<Notification> findByStatut(StatutNotification statut);

    /**
     * Trouver les notifications par canal
     */
    List<Notification> findByCanal(Canal canal);

    /**
     * Trouver les notifications d'un utilisateur par statut
     */
    List<Notification> findByUtilisateurIdAndStatut(UUID utilisateurId, StatutNotification statut);

    /**
     * Trouver les notifications d'un utilisateur par type
     */
    List<Notification> findByUtilisateurIdAndType(UUID utilisateurId, String type);

    /**
     * Trouver une notification par event ID (pour éviter les doublons)
     */
    Optional<Notification> findByEventId(String eventId);

    /**
     * Vérifier si une notification existe pour un event ID
     */
    boolean existsByEventId(String eventId);

    /**
     * Trouver les notifications envoyées dans une période
     */
    List<Notification> findByEnvoyeLeBetween(LocalDateTime debut, LocalDateTime fin);

    /**
     * Compter les notifications par statut
     */
    long countByStatut(StatutNotification statut);

    /**
     * Compter les notifications d'un utilisateur
     */
    long countByUtilisateurId(UUID utilisateurId);

    /**
     * Trouver les notifications en échec pour retry
     */
    List<Notification> findByStatutAndCreateLeBefore(StatutNotification statut, LocalDateTime date);
}
