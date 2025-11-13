package projet_nassar_microservice.user_service.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import projet_nassar_microservice.user_service.dto.RegisterRequestDto;
import projet_nassar_microservice.user_service.entity.User;
import projet_nassar_microservice.user_service.excpetion.UserNameAlreadyTakenException;
import projet_nassar_microservice.user_service.repo.UserRepo;

@Service
@AllArgsConstructor
public class AuthService {

    UserRepo  userRepo;

    public void register(RegisterRequestDto registerRequestDto) {
        //input validation

        if (userRepo.existsByEmail(registerRequestDto.getEmail())){
            throw new UserNameAlreadyTakenException("Username is already taken");
        }

        User newUser = new User();
        newUser.setEmail(registerRequestDto.getEmail());

        newUser.setFirstName(registerRequestDto.getFirstName());
        newUser.setLastName(registerRequestDto.getLastName());

    }
}
