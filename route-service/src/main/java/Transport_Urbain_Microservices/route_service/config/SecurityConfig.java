package Transport_Urbain_Microservices.route_service.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,"/run/**","/stop/**","/route/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/run/**","/stop/**","/route/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/run/**","/stop/**","/route/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,"/run/**","/stop/**","/route/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,"/run/**","/stop/**","/route/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )

                // CSRF - consider enabling in production
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        return converter;
    }

    @Bean
    public Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        return jwt -> {
            // Extract realm roles
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            Collection<String> realmRoles = Collections.emptyList();

            if (realmAccess != null && realmAccess.get("roles") != null) {
                realmRoles = (Collection<String>) realmAccess.get("roles");
            }

            // Extract resource roles (optional, if you need them later)
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

            // Convert roles to GrantedAuthority with ROLE_ prefix
            return realmRoles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        };
    }
}
