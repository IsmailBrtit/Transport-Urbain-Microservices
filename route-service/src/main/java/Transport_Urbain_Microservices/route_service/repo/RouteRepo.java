package Transport_Urbain_Microservices.route_service.repo;

import Transport_Urbain_Microservices.route_service.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteRepo extends JpaRepository<Route, Long> {
    Optional<Route> findByOsmId(Long osmId);
}
