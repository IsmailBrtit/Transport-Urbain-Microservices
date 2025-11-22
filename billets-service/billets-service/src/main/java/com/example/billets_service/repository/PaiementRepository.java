package com.example.billets_service.repository;

import com.example.billets_service.model.Paiement;
import com.example.billets_service.model.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, UUID> {
    Optional<Paiement> findByTicketId(UUID ticketId);
    List<Paiement> findByStatut(StatutPaiement statut);
}
