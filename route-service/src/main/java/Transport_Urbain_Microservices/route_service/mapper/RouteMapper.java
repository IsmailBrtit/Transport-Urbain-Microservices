package Transport_Urbain_Microservices.route_service.mapper;

import Transport_Urbain_Microservices.route_service.dto.RouteDto;
import Transport_Urbain_Microservices.route_service.entity.Route;
import Transport_Urbain_Microservices.route_service.entity.RouteStop;

import java.util.ArrayList;
import java.util.Comparator;

public class RouteMapper {

    public static RouteDto toDto(Route route) {
        RouteDto routeDto = new RouteDto();
        routeDto.setId(route.getId());
        routeDto.setName(route.getName());
        routeDto.setNum(route.getNum());
        routeDto.setDescription(route.getDescription());
        routeDto.setStatus(route.getStatus());
        routeDto.setRouteStops(new ArrayList<>());
        route.getRouteStops().stream()
                .sorted(Comparator.comparing(RouteStop::getStopOrder))
                .forEach(routeStop -> routeDto.getRouteStops().add(routeStop.getStop().getId()));
        return routeDto;
    }
}
