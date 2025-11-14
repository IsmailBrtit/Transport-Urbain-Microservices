package com.example.abonnements_service.dto;

import com.example.abonnements_service.model.Devise;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForfaitRequest {
    @NotBlank(message = "Nom is required")
    private String nom;

    @NotBlank(message = "Duree is required")
    private String duree;

    @NotNull(message = "Prix is required")
    private BigDecimal prix;

    private Devise devise;  // Default to Devise.MAD in service

    private String description;

    private Boolean actif;  // Default to true in service
}
