package com.example.abonnements_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Entity
@Table(name = "factures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facture {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID abonnementId;  // Reference to Abonnement

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;  // Invoice amount

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Devise devise;  // Currency enum for type safety

    @Column(nullable = false, unique = true)
    private String numeroFacture;  // Invoice number (e.g., FAC-2025-00123)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutFacture statut = StatutFacture.EN_ATTENTE;  // Default to PENDING

    @Column(nullable = false, updatable = false)
    private LocalDateTime emissLe;  // Issue timestamp

    @PrePersist
    protected void onCreate() {
        emissLe = LocalDateTime.now();
        if (numeroFacture == null) {
            numeroFacture = genererNumeroFacture();
        }
    }

    private String genererNumeroFacture() {
        // Format: FAC-YYYY-NNNNN
        String annee = String.valueOf(emissLe != null ? emissLe.getYear() : LocalDateTime.now().getYear());
        String numero = String.format("%05d", new Random().nextInt(99999) + 1);
        return "FAC-" + annee + "-" + numero;
    }
}
