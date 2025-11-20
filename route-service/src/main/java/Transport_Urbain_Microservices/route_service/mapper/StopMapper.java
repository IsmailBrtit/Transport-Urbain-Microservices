package Transport_Urbain_Microservices.route_service.mapper;

import Transport_Urbain_Microservices.route_service.dto.StopDto;
import Transport_Urbain_Microservices.route_service.entity.Stop;

public class StopMapper {

    public static StopDto toDto(Stop stop) {
        StopDto s = new StopDto();
        s.setId(stop.getId());
        s.setName(stop.getName());
        s.setLat(stop.getLat());
        s.setLon(stop.getLon());
        s.setAddress(stop.getAddress());
        return s;
    }

}
