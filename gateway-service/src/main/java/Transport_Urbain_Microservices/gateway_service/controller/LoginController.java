package Transport_Urbain_Microservices.gateway_service.controller;

import Transport_Urbain_Microservices.gateway_service.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

@RestController
public class LoginController {

    private final ReactiveClientRegistrationRepository clientRegistrationRepository;
    private final ServerOAuth2AuthorizedClientRepository authorizedClientRepository;
    private final ServerSecurityContextRepository securityContextRepository;
    private final WebClient webClient;
    private final String registrationId = "keycloak";

    // Inject client secret from config (ensure it's set in application.yaml as spring.security.oauth2.client.registration.keycloak.client-secret)
    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;

    public LoginController(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ServerOAuth2AuthorizedClientRepository authorizedClientRepository,
            ServerSecurityContextRepository securityContextRepository,
            WebClient.Builder webClientBuilder) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizedClientRepository = authorizedClientRepository;
        this.securityContextRepository = securityContextRepository;
        this.webClient = webClientBuilder.build();
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<String>> login(@RequestBody LoginRequest loginRequest, ServerWebExchange exchange) {
        return clientRegistrationRepository.findByRegistrationId(registrationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Client registration not found")))
                .flatMap(registration -> {
                    String tokenUri = registration.getProviderDetails().getTokenUri();
                    String clientId = registration.getClientId();
                    String scopes = String.join(" ", registration.getScopes());

                    return webClient.post()
                            .uri(tokenUri)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .body(BodyInserters.fromFormData("grant_type", "password")
                                    .with("username", loginRequest.username())
                                    .with("password", loginRequest.password())
                                    .with("client_id", clientId)
                                    .with("client_secret", clientSecret)
                                    .with("scope", scopes))
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                            .map(tokenResponse -> reactor.util.function.Tuples.of(registration, tokenResponse));
                })
                .flatMap(tuple -> {
                    ClientRegistration registration = tuple.getT1();
                    Map<String, Object> tokenResponse = tuple.getT2();

                    String idTokenValue = (String) tokenResponse.get("id_token");
                    if (idTokenValue == null) {
                        return Mono.error(new IllegalStateException("No id_token in response; ensure 'openid' scope is requested"));
                    }

                    ReactiveJwtDecoder jwtDecoder = ReactiveJwtDecoders.fromOidcIssuerLocation(registration.getProviderDetails().getIssuerUri());

                    return jwtDecoder.decode(idTokenValue)
                            .flatMap(jwt -> {
                                Map<String, Object> claims = jwt.getClaims();
                                Instant issuedAt = jwt.getIssuedAt();
                                Instant expiresAt = jwt.getExpiresAt();

                                OidcIdToken idToken = OidcIdToken.withTokenValue(idTokenValue)
                                        .claims(c -> c.putAll(claims))
                                        .issuedAt(issuedAt)
                                        .expiresAt(expiresAt)
                                        .build();

                                OidcUser oidcUser = new DefaultOidcUser(
                                        createAuthorityList("SCOPE_openid", "ROLE_USER"), // Adjust roles based on claims if needed
                                        idToken,
                                        registration.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()
                                );

                                Authentication authentication = new org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken(
                                        oidcUser,
                                        oidcUser.getAuthorities(),
                                        registration.getRegistrationId()
                                );

                                SecurityContext securityContext = new SecurityContextImpl(authentication);

                                return securityContextRepository.save(exchange, securityContext)
                                        .then(Mono.defer(() -> {
                                            String accessTokenValue = (String) tokenResponse.get("access_token");
                                            Integer expiresIn = (Integer) tokenResponse.get("expires_in");
                                            Instant accessTokenIssuedAt = Instant.now();
                                            Instant accessTokenExpiresAt = accessTokenIssuedAt.plusSeconds(expiresIn.longValue());

                                            OAuth2AccessToken accessToken = new OAuth2AccessToken(
                                                    OAuth2AccessToken.TokenType.BEARER,
                                                    accessTokenValue,
                                                    accessTokenIssuedAt,
                                                    accessTokenExpiresAt
                                            );

                                            String refreshTokenValue = (String) tokenResponse.get("refresh_token");
                                            Integer refreshExpiresIn = (Integer) tokenResponse.get("refresh_expires_in");
                                            Instant refreshTokenIssuedAt = Instant.now();
                                            Instant refreshTokenExpiresAt = refreshTokenIssuedAt.plusSeconds(refreshExpiresIn.longValue());

                                            OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(
                                                    refreshTokenValue,
                                                    refreshTokenIssuedAt,
                                                    refreshTokenExpiresAt
                                            );

                                            OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
                                                    registration,
                                                    oidcUser.getName(),
                                                    accessToken,
                                                    refreshToken
                                            );

                                            return authorizedClientRepository.saveAuthorizedClient(
                                                    authorizedClient,
                                                    authentication,
                                                    exchange
                                            );
                                        }))
                                        .thenReturn(ResponseEntity.ok("Login successful"));
                            });
                })
                .onErrorResume(e -> {
                    // Log error (e.g., invalid credentials)
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: " + e.getMessage()));
                });
    }
}