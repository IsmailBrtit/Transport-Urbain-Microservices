package com.example.abonnements_service.repository;

import com.example.abonnements_service.model.Forfait;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ForfaitRepository extends JpaRepository<Forfait, UUID> {
    List<Forfait> findByActifTrue();
    boolean existsByNom(String nom);
}
