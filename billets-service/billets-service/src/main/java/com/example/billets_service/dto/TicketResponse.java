package com.example.billets_service.dto;

import com.example.billets_service.model.Devise;
import com.example.billets_service.model.StatutTicket;
import com.example.billets_service.model.TypeTicket;
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
public class TicketResponse {
    private UUID id;
    private UUID utilisateurId;
    private TypeTicket type;
    private BigDecimal prix;
    private Devise devise;
    private LocalDateTime dateAchat;
    private StatutTicket statut;
    private String qrCode;
    private LocalDateTime validJusque;
}
