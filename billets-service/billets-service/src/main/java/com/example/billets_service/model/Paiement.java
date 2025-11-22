package com.example.billets_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity Paiement - Représente un paiement pour un ticket
 * Basé sur UML_CONCEPTION.md - BC Billets
 */
@Entity
@Table(name = "paiements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Référence au ticket (relation locale - même BC)
     */
    @Column(name = "ticket_id", nullable = false)
    private UUID ticketId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Devise devise;

    @Enumerated(EnumType.STRING)
    @Column(name = "methode_paiement", nullable = false, length = 50)
    private MethodePaiement methodePaiement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutPaiement statut;

    @Column(name = "create_le", nullable = false, updatable = false)
    private LocalDateTime createLe;

    @PrePersist
    protected void onCreate() {
        createLe = LocalDateTime.now();
        if (statut == null) {
            statut = StatutPaiement.EN_ATTENTE;
        }
    }

    /**
     * Vérifier si le paiement est réussi
     */
    public boolean isSuccessful() {
        return statut == StatutPaiement.REUSSI;
    }

    /**
     * Marquer le paiement comme réussi
     */
    public void marquerReussi() {
        this.statut = StatutPaiement.REUSSI;
    }

    /**
     * Marquer le paiement comme échoué
     */
    public void marquerEchec() {
        this.statut = StatutPaiement.ECHEC;
    }
}
