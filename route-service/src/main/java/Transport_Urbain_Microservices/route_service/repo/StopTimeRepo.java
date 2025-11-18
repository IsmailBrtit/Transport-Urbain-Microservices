package Transport_Urbain_Microservices.route_service.repo;

import Transport_Urbain_Microservices.route_service.entity.StopTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StopTimeRepo extends JpaRepository<StopTime, Long> {
}
