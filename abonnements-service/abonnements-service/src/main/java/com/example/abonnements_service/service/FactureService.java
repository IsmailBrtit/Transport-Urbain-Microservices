package com.example.abonnements_service.service;

import com.example.abonnements_service.dto.FactureResponse;
import com.example.abonnements_service.exception.ResourceNotFoundException;
import com.example.abonnements_service.model.Abonnement;
import com.example.abonnements_service.model.Facture;
import com.example.abonnements_service.model.StatutFacture;
import com.example.abonnements_service.repository.AbonnementRepository;
import com.example.abonnements_service.repository.FactureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FactureService {

    private final FactureRepository factureRepository;
    private final AbonnementRepository abonnementRepository;

    /**
     * Generate facture for an abonnement
     */
    public FactureResponse genererFacture(Abonnement abonnement) {
        log.info("Generating facture for abonnement: {}", abonnement.getId());

        // Check if facture already exists
        Optional<Facture> existingFacture = factureRepository.findByAbonnementId(abonnement.getId());
        if (existingFacture.isPresent()) {
            log.warn("Facture already exists for abonnement: {}", abonnement.getId());
            return mapToResponse(existingFacture.get());
        }

        // Create facture
        Facture facture = Facture.builder()
                .abonnementId(abonnement.getId())
                .montant(abonnement.getPrix())
                .devise(abonnement.getDevise())
                .statut(StatutFacture.EN_ATTENTE)
                .build();

        Facture saved = factureRepository.save(facture);
        log.info("Facture generated with numero: {}", saved.getNumeroFacture());

        return mapToResponse(saved);
    }

    /**
     * Get all factures
     */
    @Transactional(readOnly = true)
    public List<FactureResponse> findAll() {
        log.info("Fetching all factures");
        return factureRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Find facture by ID
     */
    @Transactional(readOnly = true)
    public Optional<FactureResponse> findById(UUID id) {
        log.info("Fetching facture with id: {}", id);
        return factureRepository.findById(id)
                .map(this::mapToResponse);
    }

    /**
     * Find facture by abonnement ID
     */
    @Transactional(readOnly = true)
    public Optional<FactureResponse> findByAbonnementId(UUID abonnementId) {
        log.info("Fetching facture for abonnement: {}", abonnementId);
        return factureRepository.findByAbonnementId(abonnementId)
                .map(this::mapToResponse);
    }

    /**
     * Get factures for multiple abonnements (for user's all invoices)
     */
    @Transactional(readOnly = true)
    public List<FactureResponse> findByAbonnementIds(List<UUID> abonnementIds) {
        log.info("Fetching factures for {} abonnements", abonnementIds.size());
        return factureRepository.findByAbonnementIdIn(abonnementIds)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update facture status (e.g., mark as paid)
     */
    public FactureResponse updateStatut(UUID id, StatutFacture nouveauStatut) {
        log.info("Updating facture {} status to: {}", id, nouveauStatut);

        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facture not found with id: " + id));

        facture.setStatut(nouveauStatut);
        Facture updated = factureRepository.save(facture);

        log.info("Facture status updated: {}", id);
        return mapToResponse(updated);
    }

    /**
     * Map entity to response DTO
     */
    private FactureResponse mapToResponse(Facture facture) {
        return FactureResponse.builder()
                .id(facture.getId())
                .abonnementId(facture.getAbonnementId())
                .montant(facture.getMontant())
                .devise(facture.getDevise())
                .numeroFacture(facture.getNumeroFacture())
                .statut(facture.getStatut())
                .emissLe(facture.getEmissLe())
                .build();
    }

}
