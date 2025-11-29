package Transport_Urbain_Microservices.route_service.dto;

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

    @NotNull(message = "Schedule type is required")
    private ScheduleType scheduleType;

    @Min(value = 1, message = "Day of week must be between 1 and 7")
    @Max(value = 7, message = "Day of week must be between 1 and 7")
    private Integer dayOfWeek;

    private LocalDate specificDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;
}
