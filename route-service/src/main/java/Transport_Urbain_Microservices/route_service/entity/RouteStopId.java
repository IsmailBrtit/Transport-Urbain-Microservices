package Transport_Urbain_Microservices.route_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteStopId implements Serializable {
    private Long route;
    private Long stop;
}
