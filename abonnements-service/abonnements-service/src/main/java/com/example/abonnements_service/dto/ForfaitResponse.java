package com.example.abonnements_service.dto;

import com.example.abonnements_service.model.Devise;
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
public class ForfaitResponse {
    private UUID id;
    private String nom;
    private String duree;
    private BigDecimal prix;
    private Devise devise;
    private String description;
    private Boolean actif;
    private LocalDateTime createLe;
}
