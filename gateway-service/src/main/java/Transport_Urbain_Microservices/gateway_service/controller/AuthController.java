package Transport_Urbain_Microservices.gateway_service.controller;

import Transport_Urbain_Microservices.gateway_service.dto.SignupRequest;
import Transport_Urbain_Microservices.gateway_service.dto.SignupResponse;
import Transport_Urbain_Microservices.gateway_service.service.UserRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserRegistrationService userRegistrationService;

    /**
     * Home endpoint - accessible without authentication
     */
    @GetMapping("/")
    public Mono<Map<String, String>> home() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to the Gateway Service");
        response.put("status", "public");
        return Mono.just(response);
    }

    /**
     * User info endpoint - requires authentication
     */
    @GetMapping("/userinfo")
    public Mono<Map<String, Object>> userInfo(
            @AuthenticationPrincipal OidcUser oidcUser,
            @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient) {

        Map<String, Object> response = new HashMap<>();
        response.put("username", oidcUser.getPreferredUsername());
        response.put("email", oidcUser.getEmail());
        response.put("name", oidcUser.getFullName());
        response.put("authorities", oidcUser.getAuthorities());
        response.put("tokenExpiry", authorizedClient.getAccessToken().getExpiresAt());

        return Mono.just(response);
    }

    /**
     * Token info endpoint - shows token details (for debugging)
     */
    @GetMapping("/tokeninfo")
    public Mono<Map<String, Object>> tokenInfo(
            @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient) {

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", authorizedClient.getAccessToken().getTokenValue());
        response.put("tokenType", authorizedClient.getAccessToken().getTokenType().getValue());
        response.put("expiresAt", authorizedClient.getAccessToken().getExpiresAt());
        response.put("scopes", authorizedClient.getAccessToken().getScopes());

        if (authorizedClient.getRefreshToken() != null) {
            response.put("hasRefreshToken", true);
            response.put("refreshTokenExpiresAt", authorizedClient.getRefreshToken().getExpiresAt());
        }

        return Mono.just(response);
    }

    @PostMapping("/public/signup")
    public Mono<ResponseEntity<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Signup request received for username: {}", request.getUsername());

        return userRegistrationService.registerUser(request)
                .map(response -> {
                    if (response.isSuccess()) {
                        log.info("User registered successfully: {}", response.getUserId());
                        return ResponseEntity.status(HttpStatus.CREATED).body(response);
                    } else {
                        log.warn("Registration failed: {}", response.getMessage());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error during signup: {}", e.getMessage(), e);
                    SignupResponse errorResponse = new SignupResponse(
                            false,
                            "Internal server error: " + e.getMessage(),
                            null,
                            null
                    );
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(errorResponse));
                });
    }

    /**
     * Endpoint to get available roles in the realm
     *
     * GET /api/auth/roles
     */
    @GetMapping("/public/roles")
    public Mono<ResponseEntity<List<String>>> getAvailableRoles() {
        return userRegistrationService.getAvailableRoles()
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error fetching roles: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(List.of()));
                });
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(WebExchangeBindException ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("success", false);
        errors.put("message", "Validation failed");

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        errors.put("errors", fieldErrors);

        return ResponseEntity.badRequest().body(errors);
    }
}
