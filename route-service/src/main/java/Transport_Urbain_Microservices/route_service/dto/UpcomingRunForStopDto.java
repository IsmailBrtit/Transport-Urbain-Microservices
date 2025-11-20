package Transport_Urbain_Microservices.route_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * This DTO is used for listing all upcoming runs for a given Stop
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingRunForStopDto {
    private Long runId;
    private String routeNum;
    private String routeName;
    private LocalDateTime arrivalTime;
    private String destination;
}
