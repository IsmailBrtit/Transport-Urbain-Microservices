package Transport_Urbain_Microservices.route_service.mapper;

import Transport_Urbain_Microservices.route_service.dto.RouteDto;
import Transport_Urbain_Microservices.route_service.entity.Route;
import Transport_Urbain_Microservices.route_service.entity.RouteStop;
import Transport_Urbain_Microservices.route_service.entity.RouteStopOffset;
import Transport_Urbain_Microservices.route_service.repo.RouteStopOffsetRepo;
import Transport_Urbain_Microservices.route_service.repo.RouteStopRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RouteMapper {

    private final RouteStopRepo routeStopRepo;
    private final RouteStopOffsetRepo routeStopOffsetRepo;

    public RouteDto toDto(Route route) {
        RouteDto dto = new RouteDto();
        dto.setId(route.getId());
        dto.setName(route.getName());
        dto.setNum(route.getNum());
        dto.setDescription(route.getDescription());
        dto.setStatus(route.getStatus());

        // Get sorted route stops
        List<RouteStop> sortedRouteStops = routeStopRepo.findByRouteOrderByStopOrderAsc(route);

        // Extract stop IDs in order
        List<Long> routeStops = sortedRouteStops.stream()
                .map(rs -> rs.getStop().getId())
                .collect(Collectors.toList());
        dto.setRouteStops(routeStops);

        // Get offsets and map by stop ID for lookup
        List<RouteStopOffset> offsets = routeStopOffsetRepo.findByRoute(route);
        Map<Long, Integer> offsetMap = offsets.stream()
                .collect(Collectors.toMap(
                        offset -> offset.getStop().getId(),
                        RouteStopOffset::getCumulativeMinutesFromStart
                ));

        // Extract cumulative minutes in the same order as stops
        List<Integer> cumulativeMinutes = sortedRouteStops.stream()
                .map(rs -> offsetMap.get(rs.getStop().getId()))
                .collect(Collectors.toList());
        dto.setCumulativeMinutesFromStartForStops(cumulativeMinutes);

        return dto;
    }
}