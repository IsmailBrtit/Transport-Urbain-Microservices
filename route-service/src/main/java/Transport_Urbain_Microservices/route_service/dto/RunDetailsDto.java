package Transport_Urbain_Microservices.route_service.dto;

import Transport_Urbain_Microservices.route_service.entity.ScheduleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RunDetailsDto {
    private Long id;
    private Long routeId;
    private String routeNum;
    private String routeName;
    private String destinationStopName;
    private ScheduleType scheduleType;
    private Integer dayOfWeek;
    private LocalDate specificDate;
    private Integer runNum;
    private LocalTime startTime;
    private List<StopTimeDetailDTO> stopTimes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StopTimeDetailDTO {
        private Long stopId;
        private String stopName;
        private Integer arrivalMinuteFromStart;
        private LocalTime actualArrivalTime;
    }
}
