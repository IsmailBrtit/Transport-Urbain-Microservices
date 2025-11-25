package Transport_Urbain_Microservices.route_service.repo;

import Transport_Urbain_Microservices.route_service.entity.Route;
import Transport_Urbain_Microservices.route_service.entity.RouteStop;
import Transport_Urbain_Microservices.route_service.entity.RouteStopId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteStopRepo extends JpaRepository<RouteStop, RouteStopId> {
    void deleteByRoute(Route route);
}
