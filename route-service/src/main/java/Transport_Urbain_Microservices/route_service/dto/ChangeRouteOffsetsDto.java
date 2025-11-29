package Transport_Urbain_Microservices.route_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ChangeRouteOffsetsDto {
    private Long id;
    private List<Integer> cumulativeMinutesFromStartForStops;
}
