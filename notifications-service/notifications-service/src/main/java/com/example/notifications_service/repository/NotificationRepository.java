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

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUtilisateurId(UUID utilisateurId);

    List<Notification> findByUtilisateurIdOrderByCreateLeDesc(UUID utilisateurId);

    List<Notification> findByType(String type);

    List<Notification> findByStatut(StatutNotification statut);

    List<Notification> findByCanal(Canal canal);

    List<Notification> findByUtilisateurIdAndStatut(UUID utilisateurId, StatutNotification statut);

    List<Notification> findByUtilisateurIdAndType(UUID utilisateurId, String type);

    Optional<Notification> findByEventId(String eventId);

    boolean existsByEventId(String eventId);

    List<Notification> findByEnvoyeLeBetween(LocalDateTime debut, LocalDateTime fin);

    long countByStatut(StatutNotification statut);

    long countByUtilisateurId(UUID utilisateurId);

    List<Notification> findByStatutAndCreateLeBefore(StatutNotification statut, LocalDateTime date);
}
