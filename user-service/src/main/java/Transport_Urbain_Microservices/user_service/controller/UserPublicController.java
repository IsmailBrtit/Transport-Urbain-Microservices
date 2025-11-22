package Transport_Urbain_Microservices.user_service.controller;

import Transport_Urbain_Microservices.user_service.dto.UserDto;
import Transport_Urbain_Microservices.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller PUBLIC pour inter-service communication
 * PAS de JWT requis - pour Abonnements, Billets, etc.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users Public API", description = "Endpoints publics pour inter-service communication")
public class UserPublicController {

    private final UserService userService;

    /**
     * Récupérer un utilisateur par ID
     * Endpoint PUBLIC - utilisé par Abonnements et Billets services
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Récupérer un utilisateur par ID",
        description = "Endpoint public pour inter-service communication. Pas d'authentification requise."
    )
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        log.info("GET /api/v1/users/{} - Appel inter-service", id);

        UserDto user = userService.getUserByIdPublic(id);
        return ResponseEntity.ok(user);
    }
}
