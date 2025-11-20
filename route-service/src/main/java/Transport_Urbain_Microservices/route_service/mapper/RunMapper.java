package Transport_Urbain_Microservices.route_service.mapper;

import Transport_Urbain_Microservices.route_service.dto.RunDetailsDto;
import Transport_Urbain_Microservices.route_service.entity.Run;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RunMapper {
    public static RunDetailsDto mapToRunDetailsDto(Run run) {
        RunDetailsDto dto = new RunDetailsDto();
        dto.setId(run.getId());
        dto.setRouteId(run.getRoute().getId());
        dto.setRouteNum(run.getRoute().getNum());
        dto.setRouteName(run.getRoute().getName());
        dto.setDirection(run.getDirection());
        dto.setDestinationStopName(run.getDestinationStopName());
        dto.setScheduleType(run.getScheduleType());
        dto.setDayOfWeek(run.getDayOfWeek());
        dto.setSpecificDate(run.getSpecificDate());
        dto.setRunNum(run.getRunNum());
        dto.setStartTime(run.getStartTime());

        List<RunDetailsDto.StopTimeDetailDTO> stopTimeDTOs = run.getStopTimes().stream()
                .map(st -> {
                    RunDetailsDto.StopTimeDetailDTO stDto = new RunDetailsDto.StopTimeDetailDTO();
                    stDto.setStopId(st.getStop().getId());
                    stDto.setStopName(st.getStop().getName());
                    stDto.setArrivalMinuteFromStart(st.getArrivalMinuteFromStart());
                    stDto.setActualArrivalTime(run.getStartTime().plusMinutes(st.getArrivalMinuteFromStart()));
                    return stDto;
                })
                .sorted(Comparator.comparing(RunDetailsDto.StopTimeDetailDTO::getArrivalMinuteFromStart))
                .collect(Collectors.toList());

        dto.setStopTimes(stopTimeDTOs);

        return dto;
    }


}
