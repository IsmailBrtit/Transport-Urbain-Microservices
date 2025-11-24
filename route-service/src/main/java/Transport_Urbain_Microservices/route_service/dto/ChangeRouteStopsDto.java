package Transport_Urbain_Microservices.route_service.dto;

import Transport_Urbain_Microservices.route_service.entity.RouteStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRouteStopsDto {
    private Long id;
    private List<Long> routeStops;
}
