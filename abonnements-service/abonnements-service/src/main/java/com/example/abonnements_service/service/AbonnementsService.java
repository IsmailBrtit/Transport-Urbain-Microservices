package com.example.abonnements_service.service;

import com.example.abonnements_service.dto.AbonnementRequest;
import com.example.abonnements_service.dto.AbonnementResponse;
import com.example.abonnements_service.model.Abonnement;
import com.example.abonnements_service.model.Forfait;
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
    private final ForfaitService forfaitService;
    private final FactureService factureService;

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

        // Fetch Forfait entity to get prix and devise (SNAPSHOT pattern)
        Forfait forfait = forfaitService.getForfaitEntityById(request.getForfaitId());

        // Check if Forfait is active
        if (!forfait.getActif()) {
            throw new IllegalStateException("Forfait is not active: " + forfait.getNom());
        }

        // Build entity with SNAPSHOT of prix/devise from Forfait
        Abonnement abonnement = Abonnement.builder()
                .utilisateurId(request.getUtilisateurId())
                .forfaitId(forfait.getId())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .prix(forfait.getPrix())          // SNAPSHOT from Forfait
                .devise(forfait.getDevise())      // SNAPSHOT from Forfait
                .statut(request.getStatut() != null ? request.getStatut() : StatutAbonnement.ACTIVE)
                .build();

        // Save Abonnement
        Abonnement saved = abonnementRepository.save(abonnement);
        log.info("Subscription created with id: {}", saved.getId());

        // Auto-generate Facture
        factureService.genererFacture(saved);
        log.info("Facture generated for abonnement: {}", saved.getId());

        // TODO: Publish AbonnementCreated event to Kafka

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
        // NOTE: prix and devise cannot be updated - they are snapshots from Forfait
        if (request.getForfaitId() != null) {
            // If changing forfait, fetch new snapshot
            Forfait newForfait = forfaitService.getForfaitEntityById(request.getForfaitId());
            if (!newForfait.getActif()) {
                throw new IllegalStateException("Cannot update to inactive forfait: " + newForfait.getNom());
            }
            abonnement.setForfaitId(newForfait.getId());
            abonnement.setPrix(newForfait.getPrix());      // Update snapshot with new forfait price
            abonnement.setDevise(newForfait.getDevise());  // Update snapshot with new forfait devise
        }
        if (request.getDateDebut() != null) {
            abonnement.setDateDebut(request.getDateDebut());
        }
        if (request.getDateFin() != null) {
            abonnement.setDateFin(request.getDateFin());
        }
        if (request.getStatut() != null) {
            abonnement.setStatut(request.getStatut());
        }

        Abonnement updated = abonnementRepository.save(abonnement);
        log.info("Subscription updated: {}", id);

        // TODO: Publish AbonnementUpdated event to kafka

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

        // TODO: Publish AbonnementCanceled event to kafka
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
     * Renew existing subscription with current Forfait prices
     */
    public AbonnementResponse renouvelerAbonnement(UUID id) {
        log.info("Renewing subscription: {}", id);

        // Get existing abonnement
        Abonnement existingAbonnement = abonnementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement not found with id: " + id));

        // Get current Forfait (with CURRENT prices, not snapshot)
        Forfait forfait = forfaitService.getForfaitEntityById(existingAbonnement.getForfaitId());

        // Check if Forfait is still active
        if (!forfait.getActif()) {
            throw new IllegalStateException("Cannot renew: Forfait is no longer active");
        }

        // Calculate new dates based on existing subscription end date
        LocalDate newDateDebut = existingAbonnement.getDateFin().plusDays(1);
        LocalDate newDateFin = calculateDateFin(newDateDebut, forfait.getDuree());

        // Create new subscription with CURRENT Forfait prices (new snapshot)
        Abonnement renewedAbonnement = Abonnement.builder()
                .utilisateurId(existingAbonnement.getUtilisateurId())
                .forfaitId(forfait.getId())
                .dateDebut(newDateDebut)
                .dateFin(newDateFin)
                .prix(forfait.getPrix())          // NEW SNAPSHOT with current price
                .devise(forfait.getDevise())      // NEW SNAPSHOT
                .statut(StatutAbonnement.ACTIVE)
                .build();

        // Save new abonnement
        Abonnement saved = abonnementRepository.save(renewedAbonnement);
        log.info("Subscription renewed with id: {}", saved.getId());

        // Generate new Facture
        factureService.genererFacture(saved);
        log.info("Facture generated for renewed abonnement: {}", saved.getId());

        // TODO: Publish AbonnementRenewed event to Kafka

        return mapToResponse(saved);
    }

    /**
     * Calculate end date based on start date and duration
     */
    private LocalDate calculateDateFin(LocalDate dateDebut, String duree) {
        // Parse duree (e.g., "30 jours", "90 jours", "365 jours")
        String[] parts = duree.split(" ");
        if (parts.length >= 2) {
            try {
                int days = Integer.parseInt(parts[0]);
                return dateDebut.plusDays(days);
            } catch (NumberFormatException e) {
                log.warn("Could not parse duree: {}, defaulting to 30 days", duree);
                return dateDebut.plusDays(30);
            }
        }
        // Default to 30 days if parsing fails
        return dateDebut.plusDays(30);
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
