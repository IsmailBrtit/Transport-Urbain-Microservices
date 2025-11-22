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
    private UUID abonnementId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Devise devise;

    @Column(nullable = false, unique = true)
    private String numeroFacture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutFacture statut = StatutFacture.EN_ATTENTE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime emissLe;

    @PrePersist
    protected void onCreate() {
        emissLe = LocalDateTime.now();
        if (numeroFacture == null) {
            numeroFacture = genererNumeroFacture();
        }
    }

    private String genererNumeroFacture() {
        String annee = String.valueOf(emissLe != null ? emissLe.getYear() : LocalDateTime.now().getYear());
        String numero = String.format("%05d", new Random().nextInt(99999) + 1);
        return "FAC-" + annee + "-" + numero;
    }
}
