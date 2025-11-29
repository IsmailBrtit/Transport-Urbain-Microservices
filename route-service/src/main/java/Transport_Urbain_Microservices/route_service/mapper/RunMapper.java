package Transport_Urbain_Microservices.route_service.mapper;
import Transport_Urbain_Microservices.route_service.dto.RunDetailsDto;
import Transport_Urbain_Microservices.route_service.entity.RouteStop;
import Transport_Urbain_Microservices.route_service.entity.RouteStopOffset;
import Transport_Urbain_Microservices.route_service.entity.Run;
import Transport_Urbain_Microservices.route_service.entity.Stop;
import Transport_Urbain_Microservices.route_service.repo.RouteStopOffsetRepo;
import Transport_Urbain_Microservices.route_service.repo.RouteStopRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RunMapper {

    private final RouteStopRepo routeStopRepository;
    private final RouteStopOffsetRepo routeStopOffsetRepository;

    public RunDetailsDto toDetailsDto(Run run) {
        RunDetailsDto dto = new RunDetailsDto();
        dto.setId(run.getId());
        dto.setRouteId(run.getRoute().getId());
        dto.setRouteNum(run.getRoute().getNum());
        dto.setRouteName(run.getRoute().getName());
        dto.setDestinationStopName(run.getDestinationStopName());
        dto.setScheduleType(run.getScheduleType());
        dto.setDayOfWeek(run.getDayOfWeek());
        dto.setSpecificDate(run.getSpecificDate());
        dto.setRunNum(run.getRunNum());
        dto.setStartTime(run.getStartTime());

        // Fetch ordered route stops for the route
        List<RouteStop> routeStops = routeStopRepository.findByRouteOrderByStopOrderAsc(run.getRoute());

        // Fetch all offsets for the route
        List<RouteStopOffset> offsets = routeStopOffsetRepository.findByRoute(run.getRoute());

        // Create a map for quick lookup of offsets by Stop
        Map<Stop, Integer> offsetMap = offsets.stream()
                .collect(Collectors.toMap(RouteStopOffset::getStop, RouteStopOffset::getCumulativeMinutesFromStart));

        // Build the list of StopTimeDetailDTO
        List<RunDetailsDto.StopTimeDetailDTO> stopTimes = new ArrayList<>();
        for (RouteStop rs : routeStops) {
            Stop stop = rs.getStop();
            Integer minutes = offsetMap.get(stop);
            if (minutes != null) {
                LocalTime arrival = run.getStartTime().plusMinutes(minutes.longValue());
                RunDetailsDto.StopTimeDetailDTO detail = new RunDetailsDto.StopTimeDetailDTO(
                        stop.getId(),
                        stop.getName(),
                        minutes,
                        arrival
                );
                stopTimes.add(detail);
            }
        }

        dto.setStopTimes(stopTimes);
        return dto;
    }
}