package Transport_Urbain_Microservices.gateway_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Configure the security filter chain
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            ReactiveClientRegistrationRepository clientRegistrationRepository) {

        http
                // Authorization rules
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/", "/public/**", "/actuator/**", "/login/**", "/error").permitAll()
                        .anyExchange().authenticated()
                )

                // OAuth2 Login with PKCE
                .oauth2Login(oauth2 -> oauth2
                        .authorizedClientRepository(authorizedClientRepository())
                )

                // OAuth2 Client configuration
                .oauth2Client(oauth2 -> oauth2
                        .authorizedClientRepository(authorizedClientRepository())
                )

                // Logout configuration
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository))
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
     * Configure OIDC logout to clear session in Keycloak
     */
    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(
            ReactiveClientRegistrationRepository clientRegistrationRepository) {

        OidcClientInitiatedServerLogoutSuccessHandler successHandler =
                new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);

        successHandler.setPostLogoutRedirectUri("{baseUrl}/");

        return successHandler;
    }
}
