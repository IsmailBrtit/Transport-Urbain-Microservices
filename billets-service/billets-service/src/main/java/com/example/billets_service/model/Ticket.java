package com.example.billets_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID utilisateurId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeTicket type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Devise devise;

    @Column(name = "date_achat", nullable = false, updatable = false)
    private LocalDateTime dateAchat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutTicket statut;

    @Column(name = "qr_code", unique = true, length = 5000)
    private String qrCode;

    @Column(name = "valid_jusque", nullable = false)
    private LocalDateTime validJusque;

    @Column(name = "create_le", nullable = false, updatable = false)
    private LocalDateTime createLe;

    @PrePersist
    protected void onCreate() {
        createLe = LocalDateTime.now();
        if (dateAchat == null) {
            dateAchat = LocalDateTime.now();
        }
        if (statut == null) {
            statut = StatutTicket.NOUVEAU;
        }
    }

    public boolean isValid() {
        return statut == StatutTicket.EMIS &&
               validJusque != null &&
               LocalDateTime.now().isBefore(validJusque);
    }

    public void valider() {
        if (!isValid()) {
            throw new IllegalStateException("Le ticket n'est pas valide pour validation");
        }
        this.statut = StatutTicket.VALIDE;
    }

    public void expirer() {
        this.statut = StatutTicket.EXPIRE;
    }
}
