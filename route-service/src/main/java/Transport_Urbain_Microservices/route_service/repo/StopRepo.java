package Transport_Urbain_Microservices.route_service.repo;

import Transport_Urbain_Microservices.route_service.entity.Stop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StopRepo extends JpaRepository<Stop, Long> {
}
