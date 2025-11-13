package projet_nassar_microservice.user_service.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import projet_nassar_microservice.user_service.entity.User;

public interface UserRepo extends JpaRepository<User, Long> {
    Boolean existsByEmail(String email);
}
