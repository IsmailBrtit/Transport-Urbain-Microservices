package com.example.notifications_service.client;

import com.example.notifications_service.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${user-service.url:http://user-service:8082}")
    private String userServiceUrl;

    public UserDto getUserById(UUID userId) {
        try {
            String url = userServiceUrl + "/api/v1/users/" + userId;
            log.debug("Calling User Service: GET {}", url);

            UserDto user = restTemplate.getForObject(url, UserDto.class);
            log.info("User récupéré avec succès: {} ({})", user.getFullName(), user.getEmail());

            return user;

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'utilisateur {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to fetch user from User Service: " + e.getMessage(), e);
        }
    }

    public String getUserEmail(UUID userId) {
        UserDto user = getUserById(userId);
        return user != null ? user.getEmail() : null;
    }
}
