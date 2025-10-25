package com.example.abonnements_service.controller;

import com.example.abonnements_service.dto.AbonnementRequest;
import com.example.abonnements_service.model.Abonnement;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/abonnements")
public class AbonnementController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createAbonnement(@RequestBody AbonnementRequest abonnementRequest) {
        // Logic to create a new abonnement
    }
}
