package com.example.abonnements_service.service;

import com.example.abonnements_service.dto.AbonnementRequest;
import com.example.abonnements_service.dto.AbonnementResponse;
import com.example.abonnements_service.event.AbonnementEvent;
import com.example.abonnements_service.event.EventType;
import com.example.abonnements_service.exception.ResourceNotFoundException;
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
    private final KafkaProducerService kafkaProducerService;
    private final com.example.abonnements_service.client.UserServiceClient userServiceClient;

    @Transactional(readOnly = true)
    public List<AbonnementResponse> findAll() {
        log.info("Fetching all subscriptions");
        return abonnementRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<AbonnementResponse> findById(UUID id) {
        log.info("Fetching subscription with id: {}", id);
        return abonnementRepository.findById(id)
                .map(this::mapToResponse);
    }

    public AbonnementResponse createAbonnement(AbonnementRequest request) {
        log.info("Creating new subscription for user: {}", request.getUtilisateurId());

        com.example.abonnements_service.dto.UserDto user = userServiceClient.getUserById(request.getUtilisateurId());
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + request.getUtilisateurId());
        }
        log.info("User verified: {} ({})", user.getUsername(), user.getEmail());

        validateDates(request.getDateDebut(), request.getDateFin());

        Forfait forfait = forfaitService.getForfaitEntityById(request.getForfaitId());

        if (!forfait.getActif()) {
            throw new IllegalStateException("Forfait is not active: " + forfait.getNom());
        }

        Abonnement abonnement = Abonnement.builder()
                .utilisateurId(request.getUtilisateurId())
                .forfaitId(forfait.getId())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .prix(forfait.getPrix())
                .devise(forfait.getDevise())
                .statut(request.getStatut() != null ? request.getStatut() : StatutAbonnement.ACTIVE)
                .build();

        Abonnement saved = abonnementRepository.save(abonnement);
        log.info("Subscription created with id: {}", saved.getId());

        var facture = factureService.genererFacture(saved);
        log.info("Facture generated for abonnement: {}", saved.getId());

        publishEvent(saved, forfait, facture.getNumeroFacture(), user, EventType.ABONNEMENT_CREATED);

        return mapToResponse(saved);
    }

    public AbonnementResponse updateAbonnement(UUID id, AbonnementRequest request) {
        log.info("Updating subscription with id: {}", id);

        Abonnement abonnement = abonnementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement not found with id: " + id));

        if (abonnement.getStatut() == StatutAbonnement.EXPIRED) {
            throw new IllegalStateException("Cannot update an expired subscription");
        }

        LocalDate newDateDebut = request.getDateDebut() != null ? request.getDateDebut() : abonnement.getDateDebut();
        LocalDate newDateFin = request.getDateFin() != null ? request.getDateFin() : abonnement.getDateFin();
        validateDates(newDateDebut, newDateFin);

        if (request.getForfaitId() != null) {
            Forfait newForfait = forfaitService.getForfaitEntityById(request.getForfaitId());
            if (!newForfait.getActif()) {
                throw new IllegalStateException("Cannot update to inactive forfait: " + newForfait.getNom());
            }
            abonnement.setForfaitId(newForfait.getId());
            abonnement.setPrix(newForfait.getPrix());
            abonnement.setDevise(newForfait.getDevise());
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

        Forfait currentForfait = forfaitService.getForfaitEntityById(updated.getForfaitId());

        return mapToResponse(updated);
    }

    public void deleteAbonnement(UUID id) {
        log.info("Deleting subscription with id: {}", id);

        Abonnement abonnement = abonnementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement not found with id: " + id));

        com.example.abonnements_service.dto.UserDto user = userServiceClient.getUserById(abonnement.getUtilisateurId());
        if (user == null) {
            log.warn("User not found for abonnement cancellation: {}", abonnement.getUtilisateurId());
        }

        abonnement.setStatut(StatutAbonnement.CANCELED);
        abonnementRepository.save(abonnement);

        log.info("Subscription canceled: {}", id);

        Forfait forfait = forfaitService.getForfaitEntityById(abonnement.getForfaitId());
        publishEvent(abonnement, forfait, null, user, EventType.ABONNEMENT_CANCELED);
    }

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

    public AbonnementResponse renouvelerAbonnement(UUID id) {
        log.info("Renewing subscription: {}", id);

        Abonnement existingAbonnement = abonnementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement not found with id: " + id));

        com.example.abonnements_service.dto.UserDto user = userServiceClient.getUserById(existingAbonnement.getUtilisateurId());
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + existingAbonnement.getUtilisateurId());
        }

        Forfait forfait = forfaitService.getForfaitEntityById(existingAbonnement.getForfaitId());

        if (!forfait.getActif()) {
            throw new IllegalStateException("Cannot renew: Forfait is no longer active");
        }

        LocalDate newDateDebut = existingAbonnement.getDateFin().plusDays(1);
        LocalDate newDateFin = calculateDateFin(newDateDebut, forfait.getDuree());

        Abonnement renewedAbonnement = Abonnement.builder()
                .utilisateurId(existingAbonnement.getUtilisateurId())
                .forfaitId(forfait.getId())
                .dateDebut(newDateDebut)
                .dateFin(newDateFin)
                .prix(forfait.getPrix())
                .devise(forfait.getDevise())
                .statut(StatutAbonnement.ACTIVE)
                .build();

        Abonnement saved = abonnementRepository.save(renewedAbonnement);
        log.info("Subscription renewed with id: {}", saved.getId());

        var facture = factureService.genererFacture(saved);
        log.info("Facture generated for renewed abonnement: {}", saved.getId());

        publishEvent(saved, forfait, facture.getNumeroFacture(), user, EventType.ABONNEMENT_RENEWED);

        return mapToResponse(saved);
    }

    private LocalDate calculateDateFin(LocalDate dateDebut, String duree) {
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
        return dateDebut.plusDays(30);
    }

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

    private void publishEvent(Abonnement abonnement, Forfait forfait, String numeroFacture,
                              com.example.abonnements_service.dto.UserDto user, EventType eventType) {
        try {
            String eventId = UUID.randomUUID().toString();

            AbonnementEvent event = AbonnementEvent.builder()
                    .eventId(eventId)
                    .eventType(eventType.name())
                    .timestamp(java.time.LocalDateTime.now())
                    .abonnementId(abonnement.getId())
                    .utilisateurId(abonnement.getUtilisateurId())
                    .utilisateurEmail(user != null ? user.getEmail() : "unknown@example.com")
                    .utilisateurNom(user != null ? user.getFullName() : "Utilisateur Inconnu")
                    .forfaitId(abonnement.getForfaitId())
                    .forfaitNom(forfait.getNom())
                    .dateDebut(abonnement.getDateDebut())
                    .dateFin(abonnement.getDateFin())
                    .prix(abonnement.getPrix())
                    .devise(abonnement.getDevise())
                    .statut(abonnement.getStatut())
                    .numeroFacture(numeroFacture)
                    .build();

            kafkaProducerService.publishAbonnementEvent(event);
            log.info("Kafka event published: {} for abonnement {} (eventId: {})",
                    eventType, abonnement.getId(), eventId);
        } catch (Exception e) {
            log.error("Failed to publish Kafka event for abonnement {}: {}", abonnement.getId(), e.getMessage());
        }
    }

}
