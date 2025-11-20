package Transport_Urbain_Microservices.route_service.service;

import Transport_Urbain_Microservices.route_service.dto.ChangeRouteStatusDto;
import Transport_Urbain_Microservices.route_service.dto.RouteDto;
import Transport_Urbain_Microservices.route_service.entity.*;
import Transport_Urbain_Microservices.route_service.mapper.RouteMapper;
import Transport_Urbain_Microservices.route_service.repo.RouteRepo;
import Transport_Urbain_Microservices.route_service.repo.RouteStopRepo;
import Transport_Urbain_Microservices.route_service.repo.StopRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepo  routeRepo;
    private final StopRepo stopRepo;
    private final RouteStopRepo routeStopRepo;

    @Transactional
    public RouteDto createRoute(RouteDto routeDto) {
        Route newRoute = new Route();
        newRoute.setName(routeDto.getName());
        newRoute.setDescription(routeDto.getDescription());
        newRoute.setNum(routeDto.getNum());

        newRoute = routeRepo.save(newRoute);

        int stopOrder = 0;
        for (Long stopId : routeDto.getRouteStops()) {
            stopOrder++;
            Stop stop = stopRepo.findById(stopId).orElseThrow(
                    ()-> new RuntimeException("Stop with id " + stopId + " not found")
            );
            RouteStop newRouteStop = new RouteStop();
            newRouteStop.setStop(stop);
            newRouteStop.setRoute(newRoute);
            newRouteStop.setOrder(stopOrder);
            routeStopRepo.save(newRouteStop);
        }
        newRoute = routeRepo.findById(newRoute.getId()).orElseThrow();
        return RouteMapper.toDto(newRoute);
    }

    @Transactional
    public RouteDto updateRoute(RouteDto routeDto) {
        Route existingRoute = routeRepo.findById(routeDto.getId()).orElseThrow(
                ()-> new RuntimeException("Route with id " + routeDto.getId() + " not found")
        );

        existingRoute.setName(routeDto.getName());
        existingRoute.setDescription(routeDto.getDescription());
        existingRoute.setNum(routeDto.getNum());

        existingRoute.getRouteStops().clear();
        int stopOrder = 0;
        for (Long stopId : routeDto.getRouteStops()) {
            stopOrder++;
            Stop stop = stopRepo.findById(stopId).orElseThrow(
                    ()-> new RuntimeException("Stop with id " + stopId + " not found")
            );
            RouteStop newRouteStop = new RouteStop();
            newRouteStop.setStop(stop);
            newRouteStop.setRoute(existingRoute);
            newRouteStop.setOrder(stopOrder);
            routeStopRepo.save(newRouteStop);
        }
        existingRoute = routeRepo.save(existingRoute);
        return RouteMapper.toDto(existingRoute);
    }

    @Transactional
    public RouteDto changeRouteStatus(ChangeRouteStatusDto changeRouteStatusDto){
        Route existingRoute = routeRepo.findById(changeRouteStatusDto.getRouteId()).orElseThrow(
                ()-> new RuntimeException("Route with id " + changeRouteStatusDto.getRouteId() + " not found")
        );
        existingRoute.setStatus(changeRouteStatusDto.getNewRouteStatus());
        return RouteMapper.toDto(routeRepo.save(existingRoute));
    }

    public RouteDto getRouteById(Long routeId) {
        Route existingRoute = routeRepo.findById(routeId).orElseThrow(
                ()-> new RuntimeException("Route with id " + routeId + " not found")
        );
        return RouteMapper.toDto(existingRoute);
    }

    public List<RouteDto> getAllRoutes(){
        List<Route> routes = routeRepo.findAll();
        return routes.stream().map(RouteMapper::toDto).toList();
    }

    public boolean deleteRouteById(Long routeId){
        try{
            routeRepo.deleteById(routeId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
