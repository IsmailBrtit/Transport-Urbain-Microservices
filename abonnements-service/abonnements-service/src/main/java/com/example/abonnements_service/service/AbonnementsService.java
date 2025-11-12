package com.example.abonnements_service.service;

import com.example.abonnements_service.dto.AbonnementRequest;
import com.example.abonnements_service.dto.AbonnementResponse;
import com.example.abonnements_service.model.Abonnement;
import com.example.abonnements_service.model.StatutAbonnement;
import com.example.abonnements_service.repository.AbonnementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AbonnementsService {

    private final AbonnementRepository abonnementRepository;

    /**
     * Get all subscriptions
     */
    @Transactional(readOnly = true)
    public List<AbonnementResponse> findAll() {
        log.info("Fetching all subscriptions");
        return abonnementRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Find subscription by ID
     */
    @Transactional(readOnly = true)
    public Optional<AbonnementResponse> findById(UUID id) {
        log.info("Fetching subscription with id: {}", id);
        return abonnementRepository.findById(id)
                .map(this::mapToResponse);
    }

    /**
     * Create new subscription
     */
    public AbonnementResponse createAbonnement(AbonnementRequest request) {
        log.info("Creating new subscription for user: {}", request.getUtilisateurId());

        // Validate dates
        validateDates(request.getDateDebut(), request.getDateFin());

        // Build entity
        Abonnement abonnement = Abonnement.builder()
                .utilisateurId(request.getUtilisateurId())
                .forfaitId(request.getForfaitId())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .prix(request.getPrix())
                .devise(request.getDevise() != null ? request.getDevise() : "MAD")
                .statut(request.getStatut() != null ? request.getStatut() : StatutAbonnement.ACTIVE)
                .build();

        // Save
        Abonnement saved = abonnementRepository.save(abonnement);
        log.info("Subscription created with id: {}", saved.getId());

        // TODO: Publish AbonnementCreated event to RabbitMQ

        return mapToResponse(saved);
    }

    /**
     * Update existing subscription
     */
    public AbonnementResponse updateAbonnement(UUID id, AbonnementRequest request) {
        log.info("Updating subscription with id: {}", id);

        Abonnement abonnement = abonnementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement not found with id: " + id));

        // Check if subscription can be updated
        if (abonnement.getStatut() == StatutAbonnement.EXPIRED) {
            throw new IllegalStateException("Cannot update an expired subscription");
        }

        // Validate dates if changed
        LocalDate newDateDebut = request.getDateDebut() != null ? request.getDateDebut() : abonnement.getDateDebut();
        LocalDate newDateFin = request.getDateFin() != null ? request.getDateFin() : abonnement.getDateFin();
        validateDates(newDateDebut, newDateFin);

        // Update fields
        if (request.getForfaitId() != null) {
            abonnement.setForfaitId(request.getForfaitId());
        }
        if (request.getDateDebut() != null) {
            abonnement.setDateDebut(request.getDateDebut());
        }
        if (request.getDateFin() != null) {
            abonnement.setDateFin(request.getDateFin());
        }
        if (request.getPrix() != null) {
            abonnement.setPrix(request.getPrix());
        }
        if (request.getDevise() != null) {
            abonnement.setDevise(request.getDevise());
        }
        if (request.getStatut() != null) {
            abonnement.setStatut(request.getStatut());
        }

        Abonnement updated = abonnementRepository.save(abonnement);
        log.info("Subscription updated: {}", id);

        // TODO: Publish AbonnementUpdated event to RabbitMQ

        return mapToResponse(updated);
    }

    /**
     * Delete (cancel) subscription
     */
    public void deleteAbonnement(UUID id) {
        log.info("Deleting subscription with id: {}", id);

        Abonnement abonnement = abonnementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement not found with id: " + id));

        // Set status to CANCELED instead of deleting
        abonnement.setStatut(StatutAbonnement.CANCELED);
        abonnementRepository.save(abonnement);

        log.info("Subscription canceled: {}", id);

        // TODO: Publish AbonnementCanceled event to RabbitMQ
    }

    /**
     * Get active subscriptions for a user
     */
    @Transactional(readOnly = true)
    public List<AbonnementResponse> getActiveAbonnementsByUserId(UUID userId) {
        log.info("Fetching active subscriptions for user: {}", userId);

        LocalDate today = LocalDate.now();

        return abonnementRepository.findByUtilisateurIdAndStatut(userId, StatutAbonnement.ACTIVE)
                .stream()
                .filter(abonnement -> !abonnement.getDateFin().isBefore(today))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validate subscription dates
     */
    private void validateDates(LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut == null || dateFin == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        if (dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        if (dateFin.equals(dateDebut)) {
            throw new IllegalArgumentException("End date must be different from start date");
        }
    }

    /**
     * Map entity to response DTO
     */
    private AbonnementResponse mapToResponse(Abonnement abonnement) {
        return AbonnementResponse.builder()
                .id(abonnement.getId())
                .utilisateurId(abonnement.getUtilisateurId())
                .forfaitId(abonnement.getForfaitId())
                .dateDebut(abonnement.getDateDebut())
                .dateFin(abonnement.getDateFin())
                .prix(abonnement.getPrix())
                .devise(abonnement.getDevise())
                .statut(abonnement.getStatut())
                .build();
    }

    /**
     * Custom exception for resource not found
     */
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}
