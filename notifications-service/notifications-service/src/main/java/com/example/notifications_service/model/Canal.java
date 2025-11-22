package com.example.notifications_service.model;

public enum Canal {
    EMAIL("Email"),
    SMS("SMS");

    private final String nom;

    Canal(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }
}
