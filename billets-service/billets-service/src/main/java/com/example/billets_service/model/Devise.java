package com.example.billets_service.model;

import lombok.Getter;

/**
 * Devises supportées pour les paiements
 */
@Getter
public enum Devise {
    MAD("Dirham Marocain", "DH"),
    EUR("Euro", "€"),
    USD("Dollar Américain", "$");

    private final String nomComplet;
    private final String symbole;

    Devise(String nomComplet, String symbole) {
        this.nomComplet = nomComplet;
        this.symbole = symbole;
    }
}
