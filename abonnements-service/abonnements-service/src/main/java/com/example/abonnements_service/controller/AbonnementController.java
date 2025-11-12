package com.example.abonnements_service.controller;

import com.example.abonnements_service.dto.AbonnementRequest;
import com.example.abonnements_service.dto.AbonnementResponse;
import com.example.abonnements_service.service.AbonnementsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/abonnements")
@RequiredArgsConstructor
public class AbonnementController {

    private final AbonnementsService abonnementsService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AbonnementResponse createAbonnement(@RequestBody AbonnementRequest abonnementRequest) {
        return abonnementsService.createAbonnement(abonnementRequest);
    }

    @GetMapping("/{id}")
    public AbonnementResponse getAbonnementById(@PathVariable UUID id) {
        return abonnementsService.findById(id)
                .orElseThrow(() -> new AbonnementsService.ResourceNotFoundException("Abonnement not found with id: " + id));
    }

    @GetMapping
    public List<AbonnementResponse> getAllAbonnements() {
        return abonnementsService.findAll();
    }

    @PutMapping("/{id}")
    public AbonnementResponse updateAbonnement(@PathVariable UUID id, @RequestBody AbonnementRequest abonnementRequest) {
        return abonnementsService.updateAbonnement(id, abonnementRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAbonnement(@PathVariable UUID id) {
        abonnementsService.deleteAbonnement(id);
    }

    @GetMapping("/active")
    public List<AbonnementResponse> getActiveAbonnementsByUserId(@RequestParam UUID userId) {
        return abonnementsService.getActiveAbonnementsByUserId(userId);
    }
}
