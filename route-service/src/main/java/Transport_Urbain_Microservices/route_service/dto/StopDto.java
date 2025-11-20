package Transport_Urbain_Microservices.route_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StopDto {
    private Long id;
    private String name;
    private Double lat;
    private Double lon;
    private String address;
}
