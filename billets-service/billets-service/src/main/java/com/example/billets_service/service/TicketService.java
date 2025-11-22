package com.example.billets_service.service;

import com.example.billets_service.client.UserServiceClient;
import com.example.billets_service.dto.TicketRequest;
import com.example.billets_service.dto.TicketResponse;
import com.example.billets_service.dto.UserDto;
import com.example.billets_service.event.TicketEvent;
import com.example.billets_service.exception.ResourceNotFoundException;
import com.example.billets_service.model.StatutTicket;
import com.example.billets_service.model.Ticket;
import com.example.billets_service.model.TypeTicket;
import com.example.billets_service.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserServiceClient userServiceClient;
    private final QRCodeService qrCodeService;
    private final KafkaProducerService kafkaProducerService;

    public TicketResponse acheterTicket(TicketRequest request) {
        log.info("Achat de ticket pour utilisateur: {}", request.getUtilisateurId());

        UserDto user = userServiceClient.getUserById(request.getUtilisateurId());
        if (user == null) {
            throw new ResourceNotFoundException("Utilisateur non trouvé: " + request.getUtilisateurId());
        }
        log.info("Utilisateur vérifié: {} ({})", user.getUsername(), user.getEmail());

        LocalDateTime validJusque = calculateValidJusque(request.getType());

        Ticket ticket = Ticket.builder()
                .utilisateurId(request.getUtilisateurId())
                .type(request.getType())
                .prix(request.getPrix())
                .devise(request.getDevise())
                .statut(StatutTicket.NOUVEAU)
                .validJusque(validJusque)
                .build();

        Ticket saved = ticketRepository.save(ticket);

        String qrCode = qrCodeService.generateQRCode(saved.getId());
        saved.setQrCode(qrCode);
        saved.setStatut(StatutTicket.EMIS);
        saved = ticketRepository.save(saved);

        log.info("Ticket créé avec ID: {} et QR code généré", saved.getId());

        publishTicketPurchasedEvent(saved, user);

        return mapToResponse(saved);
    }

    public TicketResponse validerTicket(UUID ticketId) {
        log.info("Validation du ticket: {}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket non trouvé: " + ticketId));

        if (!ticket.isValid()) {
            throw new IllegalStateException("Le ticket n'est pas valide pour validation");
        }

        ticket.valider();
        Ticket validated = ticketRepository.save(ticket);

        log.info("Ticket validé: {}", ticketId);

        UserDto user = userServiceClient.getUserById(ticket.getUtilisateurId());
        publishTicketValidatedEvent(validated, user);

        return mapToResponse(validated);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByUser(UUID userId) {
        log.info("Récupération des tickets pour utilisateur: {}", userId);
        return ticketRepository.findByUtilisateurId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(UUID ticketId) {
        log.info("Récupération du ticket: {}", ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket non trouvé: " + ticketId));
        return mapToResponse(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets() {
        log.info("Récupération de tous les tickets");
        return ticketRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private LocalDateTime calculateValidJusque(TypeTicket type) {
        LocalDateTime now = LocalDateTime.now();
        return switch (type) {
            case SIMPLE -> now.plusHours(2);
            case ALLER_RETOUR -> now.plusHours(24);
            case JOURNALIER -> now.plusHours(24);
        };
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .utilisateurId(ticket.getUtilisateurId())
                .type(ticket.getType())
                .prix(ticket.getPrix())
                .devise(ticket.getDevise())
                .dateAchat(ticket.getDateAchat())
                .statut(ticket.getStatut())
                .qrCode(ticket.getQrCode())
                .validJusque(ticket.getValidJusque())
                .build();
    }

    private void publishTicketPurchasedEvent(Ticket ticket, UserDto user) {
        try {
            TicketEvent event = TicketEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("TICKET_PURCHASED")
                    .timestamp(LocalDateTime.now())
                    .ticketId(ticket.getId())
                    .utilisateurId(ticket.getUtilisateurId())
                    .utilisateurEmail(user != null ? user.getEmail() : "unknown@example.com")
                    .utilisateurNom(user != null ? user.getFullName() : "Utilisateur Inconnu")
                    .type(ticket.getType().name())
                    .prix(ticket.getPrix())
                    .devise(ticket.getDevise().name())
                    .qrCode(ticket.getQrCode())
                    .validJusque(ticket.getValidJusque())
                    .build();

            kafkaProducerService.publishTicketEvent(event);
            log.info("Événement TICKET_PURCHASED publié (eventId: {})", event.getEventId());
        } catch (Exception e) {
            log.error("Échec de publication de l'événement: {}", e.getMessage());
        }
    }

    private void publishTicketValidatedEvent(Ticket ticket, UserDto user) {
        try {
            TicketEvent event = TicketEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("TICKET_VALIDATED")
                    .timestamp(LocalDateTime.now())
                    .ticketId(ticket.getId())
                    .utilisateurId(ticket.getUtilisateurId())
                    .utilisateurEmail(user != null ? user.getEmail() : "unknown@example.com")
                    .utilisateurNom(user != null ? user.getFullName() : "Utilisateur Inconnu")
                    .type(ticket.getType().name())
                    .prix(ticket.getPrix())
                    .devise(ticket.getDevise().name())
                    .build();

            kafkaProducerService.publishTicketEvent(event);
            log.info("Événement TICKET_VALIDATED publié (eventId: {})", event.getEventId());
        } catch (Exception e) {
            log.error("Échec de publication de l'événement: {}", e.getMessage());
        }
    }
}
