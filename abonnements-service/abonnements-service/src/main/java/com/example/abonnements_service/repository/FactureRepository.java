package com.example.abonnements_service.repository;

import com.example.abonnements_service.model.Facture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FactureRepository extends JpaRepository<Facture, UUID> {
    Optional<Facture> findByAbonnementId(UUID abonnementId);
    List<Facture> findByAbonnementIdIn(List<UUID> abonnementIds);
}
