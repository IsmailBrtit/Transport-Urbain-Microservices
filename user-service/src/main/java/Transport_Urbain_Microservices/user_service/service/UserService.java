package Transport_Urbain_Microservices.user_service.service;

import Transport_Urbain_Microservices.user_service.dto.ChangeRoleDto;
import Transport_Urbain_Microservices.user_service.dto.RegisterRequestDto;
import Transport_Urbain_Microservices.user_service.dto.UserDto;
import Transport_Urbain_Microservices.user_service.entity.User;
import Transport_Urbain_Microservices.user_service.entity.UserRole;
import Transport_Urbain_Microservices.user_service.exception.*;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import Transport_Urbain_Microservices.user_service.mapper.UserMapper;
import org.springframework.stereotype.Service;
import Transport_Urbain_Microservices.user_service.repo.UserRepo;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepo userRepo;
    private final Keycloak keycloakAdmin;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Transactional
    public UserDto createUser(RegisterRequestDto registerRequestDto) {
        if (userRepo.existsByUsername(registerRequestDto.getUsername())) {
            throw new DuplicateUsernameException("Username already exists: " + registerRequestDto.getUsername());
        }
        if (userRepo.existsByEmail(registerRequestDto.getEmail())) {
            throw new DuplicateEmailException("Email already exists: " + registerRequestDto.getEmail());
        }

        User newUser = new User();
        newUser.setUsername(registerRequestDto.getUsername());
        newUser.setEmail(registerRequestDto.getEmail());
        newUser.setFirstName(registerRequestDto.getFirstName());
        newUser.setLastName(registerRequestDto.getLastName());
        newUser.setRole(registerRequestDto.getRole());
        newUser.setPhone(registerRequestDto.getPhone());
        newUser.setEnabled(true);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepo.save(newUser);

        // Create user in Keycloak
        try {
            createKeycloakUser(savedUser,registerRequestDto.getPassword());
        } catch (Exception e) {
            log.error("Failed to create user in Keycloak: {}", e.getMessage(), e);
            throw new AuthServiceException("Failed to create user in Keycloak: " + e.getMessage());
        }

        return UserMapper.toDto(savedUser);
    }


    @Transactional(readOnly = true)
    public UserDto getUserById(Long id, Authentication authentication) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (!isAdmin(authentication) && !isSelf(user.getUsername(), authentication)) {
            throw new UnauthorizedException("You are not authorized to view this user");
        }

        return UserMapper.toDto(user);
    }


    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers(Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new UnauthorizedException("Only admins can retrieve all users");
        }

        return userRepo.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public UserDto updateUser(UserDto userDto, Authentication authentication) {
        Long id = userDto.getId();
        User existingUser = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (!isAdmin(authentication) && !isSelf(existingUser.getUsername(), authentication)) {
            throw new UnauthorizedException("You are not authorized to update this user");
        }

        // Check email uniqueness if email is being changed
        if (!existingUser.getEmail().equals(userDto.getEmail())) {
            if (userRepo.existsByEmail(userDto.getEmail())) {
                throw new DuplicateEmailException("Email already exists: " + userDto.getEmail());
            }
        }

        String userUsername = existingUser.getUsername();

        existingUser.setEmail(userDto.getEmail());
        existingUser.setFirstName(userDto.getFirstName());
        existingUser.setLastName(userDto.getLastName());
        existingUser.setPhone(userDto.getPhone());
        existingUser.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepo.save(existingUser);

        try {
            updateKeycloakUser(userUsername, updatedUser);
        } catch (Exception e) {
            log.error("Failed to update user in Keycloak: {}", e.getMessage(), e);
            throw new AuthServiceException("Failed to update user in Keycloak: " + e.getMessage());
        }

        return UserMapper.toDto(updatedUser);
    }


    @Transactional
    public UserDto changeUserRole(ChangeRoleDto changeRoleDto, Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new UnauthorizedException("Only admins can change user roles");
        }
        User user = userRepo.findById(changeRoleDto.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + changeRoleDto.getId()));

        user.setRole(changeRoleDto.getNewRole());
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepo.save(user);

        try {
            changeKeycloakUserRole(updatedUser.getUsername(), changeRoleDto.getNewRole());
        } catch (Exception e) {
            log.error("Failed to change user role in Keycloak: {}", e.getMessage(), e);
            throw new AuthServiceException("Failed to change user role in Keycloak: " + e.getMessage());
        }

        return UserMapper.toDto(updatedUser);
    }


    @Transactional
    public UserDto disableUser(Long id, Authentication authentication) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (!isAdmin(authentication) && !isSelf(user.getUsername(), authentication)) {
            throw new UnauthorizedException("You are not authorized to disable this user");
        }

        user.setEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());

        User disabledUser = userRepo.save(user);
        try {
            setKeycloakUserEnabled(disabledUser.getUsername(), false);
        } catch (Exception e) {
            log.error("Failed to disable user in Keycloak: {}", e.getMessage(), e);
            throw new AuthServiceException("Failed to disable user in Keycloak: " + e.getMessage());
        }
        return UserMapper.toDto(disabledUser);
    }


    @Transactional
    public UserDto enableUser(Long id, Authentication authentication) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (!isAdmin(authentication)) {
            throw new UnauthorizedException("You are not authorized to disable this user");
        }

        user.setEnabled(true);
        user.setUpdatedAt(LocalDateTime.now());

        User enabledUser = userRepo.save(user);

        try {
            setKeycloakUserEnabled(enabledUser.getUsername(), true);
        } catch (Exception e) {
            log.error("Failed to enable user in Keycloak: {}", e.getMessage(), e);
            throw new AuthServiceException("Failed to enable user in Keycloak: " + e.getMessage());
        }
        return UserMapper.toDto(enabledUser);
    }


    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_ADMIN"));
    }


    /**
     * Check if the authenticated user's email matches the provided email
     */
    private boolean isSelf(String username, Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String userUsername = jwt.getClaim("preferred_username");
            return username != null && username.equals(userUsername);
        }
        return false;
    }

    private void createKeycloakUser(User user, String password) {
        log.info("Creating user in Keycloak: {}", user.getEmail());

        RealmResource realmResource = keycloakAdmin.realm(realm);
        UsersResource usersResource = realmResource.users();

        // Check if user already exists in Keycloak
        if (!usersResource.search(user.getUsername()).isEmpty()) {
            log.warn("User already exists: {}", user.getUsername());
            throw new DuplicateUsernameException("User already exists in Keycloak");
        }
        if (!usersResource.searchByEmail(user.getEmail(), true).isEmpty()) {
            log.warn("User already exists in Keycloak: {}", user.getEmail());
            throw new DuplicateEmailException("User already exists in Keycloak");
        }

        // Create user representation
        UserRepresentation keycloakUser = new UserRepresentation();
        keycloakUser.setUsername(user.getUsername());
        keycloakUser.setEmail(user.getEmail());
        keycloakUser.setFirstName(user.getFirstName());
        keycloakUser.setLastName(user.getLastName());
        keycloakUser.setEnabled(user.isEnabled());
        keycloakUser.setEmailVerified(false);

        // Create user
        Response response = usersResource.create(keycloakUser);

        if (response.getStatus() != 201) {
            log.error("Failed to create user in Keycloak. Status: {}, Message: {}",
                    response.getStatus(), response.getStatusInfo());
            response.close();
            throw new AuthServiceException("Failed to create user in Keycloak: " + response.getStatusInfo());
        }

        // Extract user ID from location header
        String locationHeader = response.getHeaderString("Location");
        String keycloakUserId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
        log.info("User created in Keycloak with ID: {}", keycloakUserId);

        // Set password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        usersResource.get(keycloakUserId).resetPassword(credential);
        log.info("Password set for user: {}", keycloakUserId);
        // Assign role
        assignKeycloakRole(realmResource, keycloakUserId, user.getRole());

        response.close();
    }

    private void updateKeycloakUser(String username, User user) {
        log.info("Updating user in Keycloak: {}", username);

        RealmResource realmResource = keycloakAdmin.realm(realm);
        UsersResource usersResource = realmResource.users();

        List<UserRepresentation> users = usersResource.search(username);

        if (users.isEmpty()) {
            log.error("User not found in Keycloak: {}", username);
            throw new UserNotFoundException("User not found in Keycloak: " + username);
        }

        UserRepresentation keycloakUser = users.get(0);
        keycloakUser.setEmail(user.getEmail());
        keycloakUser.setFirstName(user.getFirstName());
        keycloakUser.setLastName(user.getLastName());

        usersResource.get(keycloakUser.getId()).update(keycloakUser);
        log.info("User updated in Keycloak: {}", user.getEmail());
    }


    private void changeKeycloakUserRole(String username, UserRole newRole) {
        log.info("Changing role for user in Keycloak: {} to {}", username, newRole);

        RealmResource realmResource = keycloakAdmin.realm(realm);
        UsersResource usersResource = realmResource.users();

        List<UserRepresentation> users = usersResource.search(username);

        if (users.isEmpty()) {
            log.error("User not found in Keycloak: {}", username);
            throw new UserNotFoundException("User not found in Keycloak: " + username);
        }

        String keycloakUserId = users.get(0).getId();

        // Remove all existing realm roles
        removeAllKeycloakRealmRoles(realmResource, keycloakUserId);

        // Assign new role
        assignKeycloakRole(realmResource, keycloakUserId, newRole);

        log.info("Role changed for user in Keycloak: {}", username);
    }

    private void assignKeycloakRole(RealmResource realmResource, String keycloakUserId, UserRole role) {
        try {
            RoleRepresentation roleRepresentation = realmResource
                    .roles()
                    .get(role.name())
                    .toRepresentation();

            realmResource
                    .users()
                    .get(keycloakUserId)
                    .roles()
                    .realmLevel()
                    .add(Collections.singletonList(roleRepresentation));

            log.info("Role {} assigned to user {} in Keycloak", role.name(), keycloakUserId);
        } catch (Exception e) {
            log.error("Failed to assign role {} to user {}: {}", role.name(), keycloakUserId, e.getMessage());
            throw new AuthServiceException("Failed to assign role in Keycloak");
        }
    }

    private void removeAllKeycloakRealmRoles(RealmResource realmResource, String keycloakUserId) {
        try {
            List<RoleRepresentation> currentRoles = realmResource
                    .users()
                    .get(keycloakUserId)
                    .roles()
                    .realmLevel()
                    .listAll();

            if (!currentRoles.isEmpty()) {
                realmResource
                        .users()
                        .get(keycloakUserId)
                        .roles()
                        .realmLevel()
                        .remove(currentRoles);

                log.info("Removed all realm roles from user {} in Keycloak", keycloakUserId);
            }
        } catch (Exception e) {
            log.error("Failed to remove roles from user {}: {}", keycloakUserId, e.getMessage());
            throw new AuthServiceException("Failed to remove roles in Keycloak");
        }
    }

    private void setKeycloakUserEnabled(String username, boolean enabled) {
        log.info("Setting enabled={} for user in Keycloak: {}", enabled, username);

        UsersResource usersResource = keycloakAdmin.realm(realm).users();
        List<UserRepresentation> users = usersResource.search(username);

        if (users.isEmpty()) {
            log.error("User not found in Keycloak: {}", username);
            throw new UserNotFoundException("User not found in Keycloak: " + username);
        }

        UserRepresentation keycloakUser = users.get(0);
        keycloakUser.setEnabled(enabled);
        usersResource.get(keycloakUser.getId()).update(keycloakUser);

        log.info("User enabled status updated in Keycloak: {}", username);
    }

}
