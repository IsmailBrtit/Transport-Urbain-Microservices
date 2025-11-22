package com.example.billets_service.model;

/**
 * Statuts possibles d'un ticket
 */
public enum StatutTicket {
    NOUVEAU,    // Ticket créé, pas encore émis
    EMIS,       // Ticket émis avec QR code
    VALIDE,     // Ticket validé/scanné
    EXPIRE,     // Ticket expiré
    ANNULE      // Ticket annulé/remboursé
}
