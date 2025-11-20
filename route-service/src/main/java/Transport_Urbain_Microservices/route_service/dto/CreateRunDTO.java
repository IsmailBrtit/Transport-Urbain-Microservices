package Transport_Urbain_Microservices.route_service.dto;

import Transport_Urbain_Microservices.route_service.entity.Direction;
import Transport_Urbain_Microservices.route_service.entity.ScheduleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRunDTO {

    @NotNull(message = "Route ID is required")
    private Long routeId;

    @NotNull(message = "Direction is required")
    private Direction direction;

    @NotNull(message = "Schedule type is required")
    private ScheduleType scheduleType;

    @Min(value = 1, message = "Day of week must be between 1 and 7")
    @Max(value = 7, message = "Day of week must be between 1 and 7")
    private Integer dayOfWeek;

    private LocalDate specificDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotEmpty(message = "Stop times list cannot be empty")
    @Valid
    private List<StopTimeDTO> stopTimes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StopTimeDTO {

        @NotNull(message = "Stop ID is required")
        private Long stopId;

        @NotNull(message = "Arrival minute from start is required")
        @Min(value = 0, message = "Arrival minute must be non-negative")
        private Integer arrivalMinuteFromStart;
    }
}
