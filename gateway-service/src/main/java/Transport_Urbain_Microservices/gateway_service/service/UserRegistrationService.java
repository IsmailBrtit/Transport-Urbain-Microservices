package Transport_Urbain_Microservices.gateway_service.service;

import Transport_Urbain_Microservices.gateway_service.dto.SignupRequest;
import Transport_Urbain_Microservices.gateway_service.dto.SignupResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import jakarta.ws.rs.core.Response;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationService {

    private final Keycloak keycloakAdmin;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Value("${keycloak.registration.default-roles:USER}")
    private String defaultRoles;

    @Value("${keycloak.registration.email-verified:false}")
    private boolean emailVerified;

    /**
     * Register a new user in Keycloak
     */
    public Mono<SignupResponse> registerUser(SignupRequest request) {
        return Mono.fromCallable(() -> {
            try {
                log.info("Attempting to register user: {}", request.getUsername());

                RealmResource realmResource = keycloakAdmin.realm(realm);
                UsersResource usersResource = realmResource.users();

                // Check if user already exists
                if (!usersResource.search(request.getUsername()).isEmpty()) {
                    log.warn("User already exists: {}", request.getUsername());
                    return new SignupResponse(false, "Username already exists", null,null);
                }

                if (!usersResource.searchByEmail(request.getEmail(), true).isEmpty()) {
                    log.warn("Email already exists: {}", request.getEmail());
                    return new SignupResponse(false, "Email already exists", null,null);
                }

                // Create user representation
                UserRepresentation user = new UserRepresentation();
                user.setUsername(request.getUsername());
                user.setEmail(request.getEmail());
                user.setFirstName(request.getFirstName());
                user.setLastName(request.getLastName());
                user.setEnabled(true);
                user.setEmailVerified(emailVerified);

                // Create user
                Response response = usersResource.create(user);
                System.out.println("response : ");
                System.out.println(response.readEntity(String.class));
                if (response.getStatus() != 201) {
                    log.error("Failed to create user. Status: {}, Message: {}",
                            response.getStatus(), response.getStatusInfo());
                    return new SignupResponse(false,
                            "Failed to create user: " + response.getStatusInfo(), null,null);
                }

                // Extract user ID from location header
                String locationHeader = response.getHeaderString("Location");
                String userId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
                log.info("User created with ID: {}", userId);

                // Set password
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(request.getPassword());
                credential.setTemporary(false);

                usersResource.get(userId).resetPassword(credential);
                log.info("Password set for user: {}", userId);

                // Assign roles
                assignRolesToUser(realmResource, userId, request.getRoles());

                response.close();

                return new SignupResponse(true, "User registered successfully", userId, user.getUsername());

            } catch (Exception e) {
                log.error("Error registering user: {}", e.getMessage(), e);
                return new SignupResponse(false,
                        "Error registering user: " + e.getMessage(), null,null);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Assign roles to a user
     */
    private void assignRolesToUser(RealmResource realmResource, String userId, Set<String> roles) {
        try {
            // Use default roles if none specified
            Set<String> rolesToAssign = (roles == null || roles.isEmpty())
                    ? Set.of(defaultRoles.split(","))
                    : roles;

            List<RoleRepresentation> roleRepresentations = new ArrayList<>();

            for (String roleName : rolesToAssign) {
                try {
                    RoleRepresentation role = realmResource.roles()
                            .get(roleName.trim())
                            .toRepresentation();
                    roleRepresentations.add(role);
                    log.info("Adding role '{}' to user {}", roleName, userId);
                } catch (Exception e) {
                    log.warn("Role '{}' not found in realm, skipping", roleName);
                }
            }

            if (!roleRepresentations.isEmpty()) {
                realmResource.users()
                        .get(userId)
                        .roles()
                        .realmLevel()
                        .add(roleRepresentations);
                log.info("Successfully assigned {} roles to user {}",
                        roleRepresentations.size(), userId);
            }

        } catch (Exception e) {
            log.error("Error assigning roles to user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Get available roles in the realm
     */
    public Mono<List<String>> getAvailableRoles() {
        return Mono.fromCallable(() -> {
            try {
                RealmResource realmResource = keycloakAdmin.realm(realm);
                return realmResource.roles().list().stream()
                        .map(RoleRepresentation::getName)
                        .filter(name -> !name.startsWith("uma_") && !name.startsWith("offline_"))
                        .toList();
            } catch (Exception e) {
                log.error("Error fetching available roles: {}", e.getMessage(), e);
                return Collections.<String>emptyList();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
