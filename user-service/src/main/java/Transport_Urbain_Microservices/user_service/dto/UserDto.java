package Transport_Urbain_Microservices.user_service.dto;

import Transport_Urbain_Microservices.user_service.entity.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String lastName;
    private String firstName;
    private UserRole role;
    private String phone;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
