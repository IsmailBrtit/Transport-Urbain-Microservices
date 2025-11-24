package Transport_Urbain_Microservices.route_service.service;

import Transport_Urbain_Microservices.route_service.dto.ChangeRouteInfoDto;
import Transport_Urbain_Microservices.route_service.dto.ChangeRouteStatusDto;
import Transport_Urbain_Microservices.route_service.dto.ChangeRouteStopsDto;
import Transport_Urbain_Microservices.route_service.dto.RouteDto;
import Transport_Urbain_Microservices.route_service.entity.*;
import Transport_Urbain_Microservices.route_service.mapper.RouteMapper;
import Transport_Urbain_Microservices.route_service.repo.RouteRepo;
import Transport_Urbain_Microservices.route_service.repo.RouteStopRepo;
import Transport_Urbain_Microservices.route_service.repo.StopRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepo  routeRepo;
    private final StopRepo stopRepo;
    private final RouteStopRepo routeStopRepo;

    @Transactional
    public RouteDto createRoute(RouteDto routeDto) {
        Route route = new Route();
        route.setName(routeDto.getName());
        route.setNum(routeDto.getNum());
        route.setDescription(routeDto.getDescription());
        route.setStatus(routeDto.getStatus() != null ? routeDto.getStatus() : RouteStatus.ACTIVE);
        route.setRouteStops(new ArrayList<>());

        List<Stop> stops = stopRepo.findAllById(routeDto.getRouteStops());

        if (stops.size() != routeDto.getRouteStops().size()) {
            throw new IllegalArgumentException("One or more stop IDs are invalid");
        }

        Map<Long, Stop> stopMap = stops.stream()
                .collect(Collectors.toMap(Stop::getId, stop -> stop));

        for (int i = 0; i < routeDto.getRouteStops().size(); i++) {
            Long stopId = routeDto.getRouteStops().get(i);
            Stop stop = stopMap.get(stopId);

            RouteStop routeStop = new RouteStop();
            routeStop.setRoute(route);
            routeStop.setStop(stop);
            routeStop.setStopOrder(i+1);

            route.getRouteStops().add(routeStop);
        }
        Route savedRoute = routeRepo.save(route);

        return RouteMapper.toDto(savedRoute);
    }

    @Transactional
    public RouteDto updateRouteInfo(ChangeRouteInfoDto changeRouteInfoDto) {
        Route existingRoute = routeRepo.findById(changeRouteInfoDto.getId()).orElseThrow(
                ()-> new RuntimeException("Route with id " + changeRouteInfoDto.getId() + " not found")
        );
        existingRoute.setName(changeRouteInfoDto.getName());
        existingRoute.setNum(changeRouteInfoDto.getNum());
        existingRoute.setDescription(changeRouteInfoDto.getDescription());
        existingRoute = routeRepo.save(existingRoute);
        return RouteMapper.toDto(existingRoute);
    }

    @Transactional
    public RouteDto updateRouteStops(ChangeRouteStopsDto changeDto) {
        Route route = routeRepo.findById(changeDto.getId()).orElseThrow(
                ()-> new RuntimeException("Route with id " + changeDto.getId() + " not found")
        );

        List<Stop> stops = stopRepo.findAllById(changeDto.getRouteStops());
        if (stops.size() != changeDto.getRouteStops().size()) {
            throw new IllegalArgumentException("One or more stop IDs are invalid");
        }

        Map<Long, Stop> stopMap = stops.stream().collect(Collectors.toMap(Stop::getId, stop -> stop));

        route.getRouteStops().clear();

        for (int i = 0; i < changeDto.getRouteStops().size(); i++) {
            Long stopId = changeDto.getRouteStops().get(i);
            Stop stop = stopMap.get(stopId);

            RouteStop routeStop = new RouteStop();
            routeStop.setRoute(route);
            routeStop.setStop(stop);
            routeStop.setStopOrder(i+1);

            route.getRouteStops().add(routeStop);
        }
        Route updatedRoute = routeRepo.save(route);
        return RouteMapper.toDto(updatedRoute);
    }

    @Transactional
    public RouteDto updateRouteStatus(ChangeRouteStatusDto changeRouteStatusDto){
        Route existingRoute = routeRepo.findById(changeRouteStatusDto.getId()).orElseThrow(
                ()-> new RuntimeException("Route with id " + changeRouteStatusDto.getId() + " not found")
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

    @Transactional
    public boolean deleteRouteById(Long routeId){
        try{
            routeRepo.deleteById(routeId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
