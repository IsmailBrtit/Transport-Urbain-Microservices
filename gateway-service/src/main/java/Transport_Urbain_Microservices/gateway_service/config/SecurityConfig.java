package Transport_Urbain_Microservices.gateway_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.security.web.server.WebFilterExchange;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;
    /**
     * Configure the security filter chain
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ServerOAuth2AuthorizedClientRepository authorizedClientRepository,
            WebClient.Builder webClientBuilder) {

        http
                // Authorization rules
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/", "/public/**","/user/register", "/actuator/**", "/login/**", "/error","/abonnements/**","/notifications/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "route/run/**", "route/stop/**", "route/route/**")
                        .permitAll().pathMatchers(HttpMethod.POST, "/login").permitAll()
                        .anyExchange().authenticated()
                )
                // OAuth2 Client configuration
                .oauth2Client(oauth2 -> oauth2
                        .authorizedClientRepository(authorizedClientRepository())
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        //.requiresLogout(ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/logout"))  // Optional: Change to POST for API safety
                        .logoutSuccessHandler(logoutSuccessHandler(clientRegistrationRepository, authorizedClientRepository, webClientBuilder))
                )
                // CSRF - consider enabling in production
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * Configure where OAuth2 tokens are stored (WebSession backed by Redis)
     */
    @Bean
    public ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new WebSessionServerOAuth2AuthorizedClientRepository();
    }
    /**
     * Custom logout handler: Revoke tokens, remove client, invalidate session, return OK response
     */
    /**
     * Custom logout handler: Revoke tokens, remove client, invalidate session, return OK response
     */
    @Bean
    public ServerLogoutSuccessHandler logoutSuccessHandler(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ServerOAuth2AuthorizedClientRepository authorizedClientRepository,
            WebClient.Builder webClientBuilder) {
        WebClient webClient = webClientBuilder.build();
        String registrationId = "keycloak";

        // Note: first parameter is WebFilterExchange (not ServerWebExchange)
        return (WebFilterExchange webFilterExchange, Authentication authentication) -> {
            ServerWebExchange exchange = webFilterExchange.getExchange();

            if (authentication == null) {
                return setResponse(exchange, HttpStatus.OK, "No active session");
            }

            return clientRegistrationRepository.findByRegistrationId(registrationId)
                    .flatMap(registration -> authorizedClientRepository.loadAuthorizedClient(registrationId, authentication, exchange)
                            .flatMap(client -> {
                                Object revocationObj = registration.getProviderDetails().getConfigurationMetadata().get("revocation_endpoint");
                                if (revocationObj == null) {
                                    // no revocation endpoint available, just remove client
                                    return authorizedClientRepository.removeAuthorizedClient(registrationId, authentication, exchange);
                                }
                                String revocationUri = revocationObj.toString();

                                String tokenToRevoke = client.getRefreshToken() != null ? client.getRefreshToken().getTokenValue() : client.getAccessToken().getTokenValue();
                                String tokenTypeHint = client.getRefreshToken() != null ? "refresh_token" : "access_token";

                                return webClient.post()
                                        .uri(revocationUri)
                                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                        .body(BodyInserters.fromFormData("client_id", registration.getClientId())
                                                .with("client_secret", clientSecret)
                                                .with("token", tokenToRevoke)
                                                .with("token_type_hint", tokenTypeHint))
                                        .retrieve()
                                        .bodyToMono(Void.class)
                                        .onErrorResume(e -> Mono.empty())
                                        .then(authorizedClientRepository.removeAuthorizedClient(registrationId, authentication, exchange));
                            })
                            .switchIfEmpty(Mono.empty())
                    )
                    .then(exchange.getSession().flatMap(session -> session.invalidate()))
                    .then(setResponse(exchange, HttpStatus.OK, "Logout successful"));
        };
    }

    private Mono<Void> setResponse(ServerWebExchange exchange, HttpStatus status, String body) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes())));
    }

    /**
     * Security context repository (Redis-backed session)
     */
    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
