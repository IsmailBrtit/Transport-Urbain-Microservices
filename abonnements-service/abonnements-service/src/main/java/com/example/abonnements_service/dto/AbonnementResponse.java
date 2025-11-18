package com.example.abonnements_service.dto;

import com.example.abonnements_service.model.Devise;
import com.example.abonnements_service.model.StatutAbonnement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbonnementResponse {
    private UUID id;
    private UUID utilisateurId;
    private UUID forfaitId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private BigDecimal prix;
    private Devise devise;
    private StatutAbonnement statut;
}