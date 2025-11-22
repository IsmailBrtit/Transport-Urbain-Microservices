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
    private String nom;

    @Column(nullable = false)
    private String duree;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Devise devise = Devise.MAD;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean actif = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createLe;

    @PrePersist
    protected void onCreate() {
        createLe = LocalDateTime.now().plusHours(1);
    }
}
