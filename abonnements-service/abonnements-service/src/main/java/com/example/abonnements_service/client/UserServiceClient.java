package com.example.abonnements_service.client;

import com.example.abonnements_service.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
@Slf4j
public class UserServiceClient {

    @Value("${user.service.url:http://localhost:8082}")
    private String userServiceUrl;

    private final RestClient restClient;

    public UserServiceClient() {
        this.restClient = RestClient.create();
    }

    public UserDto getUserById(UUID userId) {
        try {
            log.info("Appel User-Service pour récupérer utilisateur: {}", userId);

            UserDto user = restClient.get()
                    .uri(userServiceUrl + "/api/v1/users/" + userId)
                    .retrieve()
                    .body(UserDto.class);

            log.info("Utilisateur récupéré: {} ({})", user.getUsername(), user.getEmail());
            return user;

        } catch (RestClientException e) {
            log.error("Erreur lors de l'appel User-Service pour userId {}: {}", userId, e.getMessage());
            return null;
        }
    }

    public boolean userExists(UUID userId) {
        UserDto user = getUserById(userId);
        return user != null;
    }
}
