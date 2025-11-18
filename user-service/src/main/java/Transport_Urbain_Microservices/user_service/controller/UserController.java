package Transport_Urbain_Microservices.user_service.controller;

import Transport_Urbain_Microservices.user_service.dto.ChangeRoleDto;
import Transport_Urbain_Microservices.user_service.dto.RegisterRequestDto;
import Transport_Urbain_Microservices.user_service.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import Transport_Urbain_Microservices.user_service.service.UserService;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> createUser(@RequestBody RegisterRequestDto registerRequestDto) {
        UserDto createdUser = userService.createUser(registerRequestDto);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers(Authentication authentication) {
        List<UserDto> users = userService.getAllUsers(authentication);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable Long id,
            Authentication authentication) {
        UserDto user = userService.getUserById(id, authentication);
        return ResponseEntity.ok(user);
    }

    @PutMapping
    public ResponseEntity<UserDto> updateUser(
            @RequestBody UserDto userDto,
            Authentication authentication) {
        UserDto updatedUser = userService.updateUser(userDto, authentication);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/change-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> changeUserRole(
            @RequestBody ChangeRoleDto changeRoleDto,
            Authentication authentication) {
        UserDto updatedUser = userService.changeUserRole(changeRoleDto, authentication);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{id}/disable")
    public ResponseEntity<UserDto> disableUser(
            @PathVariable Long id,
            Authentication authentication) {
        UserDto disabledUser = userService.disableUser(id, authentication);
        return ResponseEntity.ok(disabledUser);
    }

    @GetMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> enableUser(
            @PathVariable Long id,
            Authentication authentication) {
        UserDto enabledUser = userService.enableUser(id, authentication);
        return ResponseEntity.ok(enabledUser);
    }
}
