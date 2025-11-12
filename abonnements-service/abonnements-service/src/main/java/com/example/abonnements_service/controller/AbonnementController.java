package com.example.abonnements_service.controller;

import com.example.abonnements_service.AbonnementsServiceApplication;
import com.example.abonnements_service.dto.AbonnementRequest;
import com.example.abonnements_service.model.Abonnement;
import com.example.abonnements_service.repository.AbonnementRepository;
import com.example.abonnements_service.service.AbonnementsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.example.abonnements_service.dto.AbonnementResponse;


import java.util.List;

@RestController
@RequestMapping("/api/v1/abonnements")
public class AbonnementController {

    private final AbonnementsService abonnementsService;

    public AbonnementController(AbonnementsService abonnementsService) {
        this.abonnementsService = abonnementsService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AbonnementResponse  createAbonnement(@RequestBody AbonnementRequest abonnementRequest) {
        return abonnementsService.createAbonnement(abonnementRequest);
    }

    @GetMapping("/{id}")
    public AbonnementResponse  getAbonnementById(@PathVariable Long id) {
        return abonnementsService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement not found", id));
    }

    //List all subscriptions for a user
    @GetMapping
    public List<AbonnementResponse> getAllAbonnements() {
        return abonnementsService.findAll();
    }


    @PutMapping("/{id}")
    public AbonnementResponse  updateAbonnement(@PathVariable Long id, @RequestBody AbonnementRequest abonnementRequest) {
        return abonnementsService.updateAbonnement(id, abonnementRequest);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAbonnement(@PathVariable Long id) {
        abonnementsService.deleteAbonnement(id);
    }

    //type
    @GetMapping("/active")
    public List<AbonnementResponse> getActiveAbonnementsByUserId(@RequestParam Long userId) {
        return abonnementsService.getActiveAbonnementsByUserId(userId);}

