package Transport_Urbain_Microservices.route_service.dto;

import Transport_Urbain_Microservices.route_service.entity.RouteStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRouteStatusDto {
    private Long routeId;
    private RouteStatus newRouteStatus;
}
