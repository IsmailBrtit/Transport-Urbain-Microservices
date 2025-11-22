package com.example.abonnements_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI abonnementsServiceOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Development server");

        Server dockerServer = new Server();
        dockerServer.setUrl("http://abonnements-service:8080");
        dockerServer.setDescription("Docker container server");

        Contact contact = new Contact();
        contact.setName("Urbain Transport Team");
        contact.setEmail("contact@urbain-transport.ma");

        License license = new License();
        license.setName("MIT License");
        license.setUrl("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Abonnements Service API")
                .version("1.0.0")
                .contact(contact)
                .description("RESTful API for managing transport subscriptions (abonnements), plans (forfaits), and invoices (factures).")
                .termsOfService("https://urbain-transport.ma/terms")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, dockerServer));
    }
}
