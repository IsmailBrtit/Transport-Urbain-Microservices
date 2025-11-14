package com.example.abonnements_service.dto;

import com.example.abonnements_service.model.StatutAbonnement;
import jakarta.validation.constraints.NotNull;
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
public class AbonnementRequest {
    @NotNull(message = "Utilisateur ID is required")
    private UUID utilisateurId;

    @NotNull(message = "Forfait ID is required")
    private UUID forfaitId;

    @NotNull(message = "Date debut is required")
    private LocalDate dateDebut;

    @NotNull(message = "Date fin is required")
    private LocalDate dateFin;

    private StatutAbonnement statut; // Optional - defaults to ACTIVE
}
