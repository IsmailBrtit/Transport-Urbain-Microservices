package Transport_Urbain_Microservices.user_service.mapper;

import Transport_Urbain_Microservices.user_service.dto.UserDto;
import Transport_Urbain_Microservices.user_service.entity.User;

public class UserMapper {
    public static UserDto toDto(User user){
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setLastName(user.getLastName());
        userDto.setFirstName(user.getFirstName());
        userDto.setEnabled(user.isEnabled());
        userDto.setRole(user.getRole());
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setUpdatedAt(user.getUpdatedAt());
        userDto.setPhone(user.getPhone());
        return userDto;
    }
}
