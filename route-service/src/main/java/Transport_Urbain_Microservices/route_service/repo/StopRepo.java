package Transport_Urbain_Microservices.route_service.repo;

import Transport_Urbain_Microservices.route_service.entity.Stop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StopRepo extends JpaRepository<Stop, Long> {
    Optional<Stop> findByOsmId(Long osmId);
}
