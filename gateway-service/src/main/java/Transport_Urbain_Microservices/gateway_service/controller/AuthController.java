package Transport_Urbain_Microservices.gateway_service.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

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
}
