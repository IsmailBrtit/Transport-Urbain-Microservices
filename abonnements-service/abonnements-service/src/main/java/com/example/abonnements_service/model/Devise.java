package com.example.abonnements_service.model;

/**
 * Enum for supported currencies
 * MAD is the primary currency for Moroccan urban transport
 */
public enum Devise {
    MAD("Dirham Marocain", "MAD"),
    EUR("Euro", "€"),
    USD("Dollar Américain", "$");

    private final String nomComplet;
    private final String symbole;

    Devise(String nomComplet, String symbole) {
        this.nomComplet = nomComplet;
        this.symbole = symbole;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public String getSymbole() {
        return symbole;
    }
}
