package com.example.notifications_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour tester l'envoi d'emails
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestEmailRequest {

    @NotBlank(message = "L'adresse email est requise")
    @Email(message = "Format d'email invalide")
    private String destinataire;

    @NotBlank(message = "Le nom du destinataire est requis")
    private String nom;

    private String message;
}
