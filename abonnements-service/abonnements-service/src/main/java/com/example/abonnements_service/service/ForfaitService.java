package com.example.abonnements_service.service;

import com.example.abonnements_service.dto.ForfaitRequest;
import com.example.abonnements_service.dto.ForfaitResponse;
import com.example.abonnements_service.exception.ResourceNotFoundException;
import com.example.abonnements_service.model.Devise;
import com.example.abonnements_service.model.Forfait;
import com.example.abonnements_service.repository.ForfaitRepository;
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
public class ForfaitService {

    private final ForfaitRepository forfaitRepository;

    @Transactional(readOnly = true)
    public List<ForfaitResponse> findAll() {
        log.info("Fetching all forfaits");
        return forfaitRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ForfaitResponse> findAllActive() {
        log.info("Fetching all active forfaits");
        return forfaitRepository.findByActifTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ForfaitResponse> findById(UUID id) {
        log.info("Fetching forfait with id: {}", id);
        return forfaitRepository.findById(id)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Forfait getForfaitEntityById(UUID id) {
        return forfaitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Forfait not found with id: " + id));
    }

    public ForfaitResponse createForfait(ForfaitRequest request) {
        log.info("Creating new forfait: {}", request.getNom());

        if (forfaitRepository.existsByNom(request.getNom())) {
            throw new IllegalArgumentException("Forfait with name '" + request.getNom() + "' already exists");
        }

        Forfait forfait = Forfait.builder()
                .nom(request.getNom())
                .duree(request.getDuree())
                .prix(request.getPrix())
                .devise(request.getDevise() != null ? request.getDevise() : Devise.MAD)
                .description(request.getDescription())
                .actif(request.getActif() != null ? request.getActif() : true)
                .build();

        Forfait saved = forfaitRepository.save(forfait);
        log.info("Forfait created with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    public ForfaitResponse updateForfait(UUID id, ForfaitRequest request) {
        log.info("Updating forfait with id: {}", id);

        Forfait forfait = forfaitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Forfait not found with id: " + id));

        if (request.getNom() != null && !request.getNom().equals(forfait.getNom())) {
            if (forfaitRepository.existsByNom(request.getNom())) {
                throw new IllegalArgumentException("Forfait with name '" + request.getNom() + "' already exists");
            }
            forfait.setNom(request.getNom());
        }

        if (request.getDuree() != null) {
            forfait.setDuree(request.getDuree());
        }
        if (request.getPrix() != null) {
            forfait.setPrix(request.getPrix());
        }
        if (request.getDevise() != null) {
            forfait.setDevise(request.getDevise());
        }
        if (request.getDescription() != null) {
            forfait.setDescription(request.getDescription());
        }
        if (request.getActif() != null) {
            forfait.setActif(request.getActif());
        }

        Forfait updated = forfaitRepository.save(forfait);
        log.info("Forfait updated: {}", id);

        return mapToResponse(updated);
    }

    public void deleteForfait(UUID id) {
        log.info("Deactivating forfait with id: {}", id);

        Forfait forfait = forfaitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Forfait not found with id: " + id));

        forfait.setActif(false);
        forfaitRepository.save(forfait);

        log.info("Forfait deactivated: {}", id);
    }

    private ForfaitResponse mapToResponse(Forfait forfait) {
        return ForfaitResponse.builder()
                .id(forfait.getId())
                .nom(forfait.getNom())
                .duree(forfait.getDuree())
                .prix(forfait.getPrix())
                .devise(forfait.getDevise())
                .description(forfait.getDescription())
                .actif(forfait.getActif())
                .createLe(forfait.getCreateLe())
                .build();
    }

}
