package com.example.billets_service.repository;

import com.example.billets_service.model.StatutTicket;
import com.example.billets_service.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByUtilisateurId(UUID utilisateurId);
    List<Ticket> findByUtilisateurIdAndStatut(UUID utilisateurId, StatutTicket statut);
    Optional<Ticket> findByQrCode(String qrCode);
    List<Ticket> findByStatutAndValidJusqueBefore(StatutTicket statut, LocalDateTime date);
}
