package com.example.abonnements_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "forfaits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Forfait {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nom;  // e.g., "Mensuel Standard", "Annuel Premium"

    @Column(nullable = false)
    private String duree;  // e.g., "30 jours", "90 jours", "365 jours"

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;  // Current plan price (can change over time)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Devise devise = Devise.MAD;  // Currency (MAD by default for Morocco)

    @Column(length = 500)
    private String description;  // Plan details and features

    @Column(nullable = false)
    @Builder.Default
    private Boolean actif = true;  // Is plan available for purchase?

    @Column(nullable = false, updatable = false)
    private LocalDateTime createLe;  // Creation timestamp

    @PrePersist
//    protected void onCreate() {
//        createLe = LocalDateTime.now()+2;
//    }
    protected void onCreate() {
        createLe = LocalDateTime.now().plusHours(1);
    }
}
