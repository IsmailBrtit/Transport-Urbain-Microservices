package Transport_Urbain_Microservices.route_service.mapper;

import Transport_Urbain_Microservices.route_service.dto.RouteDto;
import Transport_Urbain_Microservices.route_service.entity.Route;
import Transport_Urbain_Microservices.route_service.entity.RouteStop;

import java.util.Comparator;

public class RouteMapper {

    public static RouteDto toDto(Route route) {
        RouteDto routeDto = new RouteDto();
        routeDto.setId(route.getId());
        routeDto.setName(route.getName());
        routeDto.setNum(route.getNum());
        routeDto.setDescription(route.getDescription());
        route.getRouteStops().stream()
                .sorted(Comparator.comparing(r -> r.getStop().getId()))
                .forEach(routeStop -> routeDto.getRouteStops().add(routeStop.getStop().getId()));
        return routeDto;
    }
}
