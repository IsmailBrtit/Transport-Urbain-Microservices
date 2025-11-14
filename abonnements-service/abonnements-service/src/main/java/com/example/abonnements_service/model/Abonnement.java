package com.example.abonnements_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.lang.annotation.Documented;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "abonnements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Abonnement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Auto-increment strategy
    private UUID id;

    @Column(nullable = false)
    private UUID utilisateurId;

    @Column(nullable = false)
    private UUID forfaitId;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    @Column(nullable = false)
    private BigDecimal prix;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Devise devise;

    @Enumerated(EnumType.STRING)
    private StatutAbonnement statut;

}
