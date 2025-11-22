package Transport_Urbain_Microservices.user_service.dto;

import Transport_Urbain_Microservices.user_service.entity.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ChangeRoleDto {
    UUID id;
    UserRole newRole;
}
