package com.example.notifications_service.service;

import com.example.notifications_service.event.AbonnementEvent;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Service d'envoi d'emails
 * Utilise JavaMailSender (Spring Boot) et Thymeleaf pour les templates HTML
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.properties.mail.smtp.from:noreply@urbain-transport.ma}")
    private String fromAddress;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Envoyer un email avec template Thymeleaf
     *
     * @param to           Adresse email destinataire
     * @param subject      Sujet de l'email
     * @param templateName Nom du template Thymeleaf (sans .html)
     * @param variables    Variables à injecter dans le template
     */
    @Async
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            log.info("Envoi d'email à {} avec template {}", to, templateName);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);

            // Générer le contenu HTML avec Thymeleaf
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

    /**
     * Envoyer un email de confirmation de création d'abonnement
     */
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

    /**
     * Envoyer un email de renouvellement d'abonnement
     */
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

    /**
     * Envoyer un email d'annulation d'abonnement
     */
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

    /**
     * Envoyer un email d'expiration d'abonnement
     */
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

    /**
     * Envoyer un email de test
     */
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

    // ==================== Helper Methods ====================

    /**
     * Formater une date au format français
     */
    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "N/A";
    }

    /**
     * Formater un montant avec devise
     */
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
