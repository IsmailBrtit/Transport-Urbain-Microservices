package Transport_Urbain_Microservices.route_service.repo;

import Transport_Urbain_Microservices.route_service.entity.Run;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunRepo extends JpaRepository<Run, Long> {
}
