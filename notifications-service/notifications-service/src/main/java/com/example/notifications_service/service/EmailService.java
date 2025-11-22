package com.example.notifications_service.service;

import com.example.notifications_service.event.AbonnementEvent;
import com.example.notifications_service.event.TicketEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import org.springframework.core.io.ByteArrayResource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.properties.mail.smtp.from:noreply@urbain-transport.ma}")
    private String fromAddress;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Async
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            log.info("Envoi d'email à {} avec template {}", to, templateName);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process("email/" + templateName, context);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email envoyé avec succès à {}", to);

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi d'email à {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Échec d'envoi d'email: " + e.getMessage(), e);
        }
    }

    @Async
    public void sendAbonnementCreatedEmail(AbonnementEvent event) {
        log.info("Envoi d'email de confirmation d'abonnement créé pour utilisateur {}", event.getUtilisateurId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("nomUtilisateur", event.getUtilisateurNom());
        variables.put("nomForfait", event.getForfaitNom());
        variables.put("dateDebut", formatDate(event.getDateDebut()));
        variables.put("dateFin", formatDate(event.getDateFin()));
        variables.put("prix", formatMontant(event.getPrix(), event.getDevise()));
        variables.put("numeroFacture", event.getNumeroFacture());

        sendEmail(
                event.getUtilisateurEmail(),
                "Confirmation de votre abonnement Urbain",
                "abonnement/created",
                variables
        );
    }

    @Async
    public void sendAbonnementRenewedEmail(AbonnementEvent event) {
        log.info("Envoi d'email de renouvellement d'abonnement pour utilisateur {}", event.getUtilisateurId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("nomUtilisateur", event.getUtilisateurNom());
        variables.put("nomForfait", event.getForfaitNom());
        variables.put("dateDebut", formatDate(event.getDateDebut()));
        variables.put("dateFin", formatDate(event.getDateFin()));
        variables.put("prix", formatMontant(event.getPrix(), event.getDevise()));
        variables.put("numeroFacture", event.getNumeroFacture());

        sendEmail(
                event.getUtilisateurEmail(),
                "Renouvellement de votre abonnement Urbain",
                "abonnement/renewed",
                variables
        );
    }

    @Async
    public void sendAbonnementCanceledEmail(AbonnementEvent event) {
        log.info("Envoi d'email d'annulation d'abonnement pour utilisateur {}", event.getUtilisateurId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("nomUtilisateur", event.getUtilisateurNom());
        variables.put("nomForfait", event.getForfaitNom());
        variables.put("dateFin", formatDate(event.getDateFin()));

        sendEmail(
                event.getUtilisateurEmail(),
                "Annulation de votre abonnement Urbain",
                "abonnement/canceled",
                variables
        );
    }

    @Async
    public void sendAbonnementExpiredEmail(AbonnementEvent event) {
        log.info("Envoi d'email d'expiration d'abonnement pour utilisateur {}", event.getUtilisateurId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("nomUtilisateur", event.getUtilisateurNom());
        variables.put("nomForfait", event.getForfaitNom());
        variables.put("dateFin", formatDate(event.getDateFin()));

        sendEmail(
                event.getUtilisateurEmail(),
                "Votre abonnement Urbain a expiré",
                "abonnement/expired",
                variables
        );
    }

    @Async
    public void sendTestEmail(String to, String nom, String message) {
        log.info("Envoi d'email de test à {}", to);

        Map<String, Object> variables = new HashMap<>();
        variables.put("nom", nom);
        variables.put("message", message != null ? message : "Ceci est un email de test du système de notifications Urbain.");

        sendEmail(
                to,
                "Email de test - Système de Notifications Urbain",
                "test",
                variables
        );
    }

    @Async
    public void sendTicketPurchasedEmail(TicketEvent event) {
        log.info("Envoi d'email d'achat de billet pour utilisateur {}", event.getUtilisateurId());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(event.getUtilisateurEmail());
            helper.setSubject("Confirmation d'achat de billet - Urbain Transport");

            Map<String, Object> variables = new HashMap<>();
            variables.put("nomUtilisateur", event.getUtilisateurNom());
            variables.put("ticketId", event.getTicketId().toString());
            variables.put("typeTicket", event.getType());
            variables.put("prix", formatMontant(event.getPrix(), event.getDevise()));
            variables.put("validJusque", formatDateTime(event.getValidJusque()));

            boolean hasQrCode = event.getQrCode() != null && event.getQrCode().contains("base64,");
            variables.put("hasQrCode", hasQrCode);
            if (hasQrCode) {
                variables.put("qrCodeCid", "qrcode");
            }

            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process("email/ticket/purchased", context);
            helper.setText(htmlContent, true);

            if (hasQrCode) {
                String base64Data = event.getQrCode().split(",")[1];
                byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                helper.addInline("qrcode", new ByteArrayResource(imageBytes), "image/png");
                log.info("QR code embarqué dans l'email");
            }

            mailSender.send(message);
            log.info("Email envoyé avec succès à {}", event.getUtilisateurEmail());

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi d'email: {}", e.getMessage(), e);
            throw new RuntimeException("Échec d'envoi d'email: " + e.getMessage(), e);
        }
    }

    @Async
    public void sendTicketValidatedEmail(TicketEvent event) {
        log.info("Envoi d'email de validation de billet pour utilisateur {}", event.getUtilisateurId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("nomUtilisateur", event.getUtilisateurNom());
        variables.put("ticketId", event.getTicketId().toString());
        variables.put("typeTicket", event.getType());
        variables.put("validJusque", formatDateTime(event.getValidJusque()));

        sendEmail(
                event.getUtilisateurEmail(),
                "Validation de votre billet - Urbain Transport",
                "ticket/validated",
                variables
        );
    }

    @Async
    public void sendTicketExpiredEmail(TicketEvent event) {
        log.info("Envoi d'email d'expiration de billet pour utilisateur {}", event.getUtilisateurId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("nomUtilisateur", event.getUtilisateurNom());
        variables.put("ticketId", event.getTicketId().toString());
        variables.put("typeTicket", event.getType());

        sendEmail(
                event.getUtilisateurEmail(),
                "Votre billet a expiré - Urbain Transport",
                "ticket/expired",
                variables
        );
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "N/A";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
    }

    private String formatMontant(BigDecimal montant, String devise) {
        if (montant == null) {
            return "N/A";
        }

        String symbol = switch (devise) {
            case "MAD" -> "DH";
            case "EUR" -> "€";
            case "USD" -> "$";
            default -> devise;
        };

        return String.format("%.2f %s", montant, symbol);
    }
}
