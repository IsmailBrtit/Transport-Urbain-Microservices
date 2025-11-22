package com.example.billets_service.controller;

import com.example.billets_service.dto.TicketRequest;
import com.example.billets_service.dto.TicketResponse;
import com.example.billets_service.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tickets", description = "API de gestion des billets de transport")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @Operation(summary = "Acheter un ticket", description = "Créer un nouveau ticket avec QR code")
    public ResponseEntity<TicketResponse> acheterTicket(@Valid @RequestBody TicketRequest request) {
        log.info("POST /api/v1/tickets - Achat de ticket pour utilisateur: {}", request.getUtilisateurId());
        TicketResponse response = ticketService.acheterTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/validate")
    @Operation(summary = "Valider un ticket", description = "Valider/scanner le QR code d'un ticket")
    public ResponseEntity<TicketResponse> validerTicket(@PathVariable UUID id) {
        log.info("POST /api/v1/tickets/{}/validate - Validation du ticket", id);
        TicketResponse response = ticketService.validerTicket(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Lister tous les tickets", description = "Récupérer tous les tickets")
    public ResponseEntity<List<TicketResponse>> getAllTickets() {
        log.info("GET /api/v1/tickets - Récupération de tous les tickets");
        List<TicketResponse> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un ticket par ID", description = "Obtenir les détails d'un ticket spécifique")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable UUID id) {
        log.info("GET /api/v1/tickets/{} - Récupération du ticket", id);
        TicketResponse ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Tickets d'un utilisateur", description = "Récupérer tous les tickets d'un utilisateur")
    public ResponseEntity<List<TicketResponse>> getTicketsByUser(@PathVariable UUID userId) {
        log.info("GET /api/v1/tickets/user/{} - Récupération des tickets utilisateur", userId);
        List<TicketResponse> tickets = ticketService.getTicketsByUser(userId);
        return ResponseEntity.ok(tickets);
    }
}
