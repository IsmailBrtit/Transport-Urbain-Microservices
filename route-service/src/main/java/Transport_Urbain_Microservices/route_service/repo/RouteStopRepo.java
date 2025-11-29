package Transport_Urbain_Microservices.route_service.repo;

import Transport_Urbain_Microservices.route_service.entity.Route;
import Transport_Urbain_Microservices.route_service.entity.RouteStop;
import Transport_Urbain_Microservices.route_service.entity.RouteStopId;
import Transport_Urbain_Microservices.route_service.entity.Stop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RouteStopRepo extends JpaRepository<RouteStop, RouteStopId> {
    void deleteByRoute(Route route);

    List<RouteStop> findByRouteOrderByStopOrderAsc(Route route);

    List<RouteStop> findByStop(Stop stop);
}
