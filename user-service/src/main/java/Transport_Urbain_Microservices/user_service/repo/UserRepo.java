package Transport_Urbain_Microservices.user_service.repo;

import Transport_Urbain_Microservices.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User,Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
