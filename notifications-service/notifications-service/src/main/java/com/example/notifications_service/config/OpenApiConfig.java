package com.example.notifications_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration Swagger/OpenAPI
 * Documentation interactive de l'API: http://localhost:8086/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationsServiceOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8086");
        localServer.setDescription("Serveur local");

        Server dockerServer = new Server();
        dockerServer.setUrl("http://notifications-service:8086");
        dockerServer.setDescription("Serveur Docker");

        Contact contact = new Contact();
        contact.setName("Équipe Urbain");
        contact.setEmail("contact@urbain-transport.ma");

        Info info = new Info()
                .title("API Notifications Service - Système de Transport Urbain")
                .version("1.0.0")
                .description("Service de notifications par email pour la plateforme de transport urbain Urbain. " +
                        "Ce service écoute les événements Kafka de tous les microservices et envoie des notifications " +
                        "par email aux utilisateurs.")
                .contact(contact);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, dockerServer));
    }
}
