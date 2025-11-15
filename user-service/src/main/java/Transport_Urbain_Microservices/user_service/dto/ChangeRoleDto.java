package Transport_Urbain_Microservices.user_service.dto;

import Transport_Urbain_Microservices.user_service.entity.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeRoleDto {
    Long id;
    UserRole newRole;
}
