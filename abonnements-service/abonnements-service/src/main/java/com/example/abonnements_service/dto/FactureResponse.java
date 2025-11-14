package com.example.abonnements_service.dto;

import com.example.abonnements_service.model.Devise;
import com.example.abonnements_service.model.StatutFacture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactureResponse {
    private UUID id;
    private UUID abonnementId;
    private BigDecimal montant;
    private Devise devise;
    private String numeroFacture;
    private StatutFacture statut;
    private LocalDateTime emissLe;
}
