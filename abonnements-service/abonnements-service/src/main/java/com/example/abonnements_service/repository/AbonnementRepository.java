package com.example.abonnements_service.repository;

import com.example.abonnements_service.model.Abonnement;
import com.example.abonnements_service.model.StatutAbonnement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AbonnementRepository extends JpaRepository<Abonnement, UUID> {
    List<Abonnement> findByUtilisateurId(UUID utilisateurId);
    List<Abonnement> findByUtilisateurIdAndStatut(UUID utilisateurId, StatutAbonnement statut);
}