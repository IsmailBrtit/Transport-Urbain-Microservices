package com.example.abonnements_service.controller;

import com.example.abonnements_service.dto.FactureResponse;
import com.example.abonnements_service.model.StatutFacture;
import com.example.abonnements_service.service.FactureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/factures")
@RequiredArgsConstructor
public class FactureController {

    private final FactureService factureService;

    @GetMapping("/{id}")
    public FactureResponse getFactureById(@PathVariable UUID id) {
        return factureService.findById(id)
                .orElseThrow(() -> new FactureService.ResourceNotFoundException("Facture not found with id: " + id));
    }

    @GetMapping
    public List<FactureResponse> getAllFactures() {
        return factureService.findAll();
    }

    @GetMapping("/abonnement/{abonnementId}")
    public FactureResponse getFactureByAbonnementId(@PathVariable UUID abonnementId) {
        return factureService.findByAbonnementId(abonnementId)
                .orElseThrow(() -> new FactureService.ResourceNotFoundException("Facture not found for abonnement: " + abonnementId));
    }

    @PatchMapping("/{id}/statut")
    public FactureResponse updateFactureStatut(@PathVariable UUID id, @RequestParam StatutFacture statut) {
        return factureService.updateStatut(id, statut);
    }
}
