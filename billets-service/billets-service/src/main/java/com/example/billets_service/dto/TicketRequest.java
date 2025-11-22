package com.example.billets_service.dto;

import com.example.billets_service.model.Devise;
import com.example.billets_service.model.TypeTicket;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequest {

    @NotNull(message = "L'utilisateur est obligatoire")
    private UUID utilisateurId;

    @NotNull(message = "Le type de ticket est obligatoire")
    private TypeTicket type;

    @NotNull(message = "Le prix est obligatoire")
    private BigDecimal prix;

    private Devise devise = Devise.MAD;
}
