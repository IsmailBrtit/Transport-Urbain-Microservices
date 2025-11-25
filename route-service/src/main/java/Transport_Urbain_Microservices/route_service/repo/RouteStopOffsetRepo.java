package Transport_Urbain_Microservices.route_service.repo;

import Transport_Urbain_Microservices.route_service.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteStopOffsetRepo extends JpaRepository<RouteStopOffset, RouteStopId> {
    Optional<RouteStopOffset> findByRouteAndStop(Route route, Stop stop);
}
