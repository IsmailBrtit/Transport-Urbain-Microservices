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
    @NotNull
    private UUID utilisateurId;
    @NotNull
    private Long forfaitId;
    @NotNull
    private LocalDate dateDebut;
    @NotNull
    private LocalDate dateFin;
    private StatutAbonnement statut; // Optional - defaults to ACTIVE
    @NotNull
    private BigDecimal prix;
    private String devise;
}
