package Transport_Urbain_Microservices.route_service.service;

import Transport_Urbain_Microservices.route_service.dto.*;
import Transport_Urbain_Microservices.route_service.entity.*;
import Transport_Urbain_Microservices.route_service.mapper.RouteMapper;
import Transport_Urbain_Microservices.route_service.repo.RouteRepo;
import Transport_Urbain_Microservices.route_service.repo.RouteStopOffsetRepo;
import Transport_Urbain_Microservices.route_service.repo.RouteStopRepo;
import Transport_Urbain_Microservices.route_service.repo.StopRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepo  routeRepo;
    private final StopRepo stopRepo;
    private final RouteStopRepo routeStopRepo;
    private final RouteStopOffsetRepo  routeStopOffsetRepo;
    private final RouteMapper routeMapper;

    @Transactional
    public RouteDto createRoute(RouteDto routeDto) {
        Route route = new Route();
        route.setName(routeDto.getName());
        route.setNum(routeDto.getNum());
        route.setDescription(routeDto.getDescription());
        route.setStatus(routeDto.getStatus() != null ? routeDto.getStatus() : RouteStatus.ACTIVE);
        route.setRouteStops(new ArrayList<>());

        if (routeDto.getRouteStops().size() != routeDto.getCumulativeMinutesFromStartForStops().size()) {
            throw new IllegalArgumentException("Cumulative minutes list must match the number of stops in the route");
        }

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

        List<RouteStop> sortedStops = savedRoute.getRouteStops().stream()
                .sorted(Comparator.comparing(RouteStop::getStopOrder))
                .toList();

        for (int i = 0; i < routeDto.getCumulativeMinutesFromStartForStops().size()-1; i++) {
            if (routeDto.getCumulativeMinutesFromStartForStops().get(i)> routeDto.getCumulativeMinutesFromStartForStops().get(i+1)){
                throw new IllegalArgumentException("CumulativeMinutesFromStart must be in ascending order");
            }
        }

        for (int i = 0; i < sortedStops.size(); i++) {
            RouteStop rs = sortedStops.get(i);
            Integer c = routeDto.getCumulativeMinutesFromStartForStops().get(i);
            RouteStopOffset offset = new RouteStopOffset(rs.getRoute(), rs.getStop(), c);
            routeStopOffsetRepo.save(offset);
        }

        return routeMapper.toDto(savedRoute);
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
        return routeMapper.toDto(existingRoute);
    }

    @Transactional
    public RouteDto updateRouteStops(ChangeRouteStopsDto changeDto) {
        if (changeDto.getRouteStops().size() != changeDto.getCumulativeMinutesFromStartForStops().size()) {
            throw new IllegalArgumentException("Route stops and cumulative minutes lists must be the same size");
        }

        Route route = routeRepo.findById(changeDto.getId()).orElseThrow(
                ()-> new RuntimeException("Route with id " + changeDto.getId() + " not found")
        );


        for (int i = 0; i < changeDto.getCumulativeMinutesFromStartForStops().size()-1; i++) {
            if (changeDto.getCumulativeMinutesFromStartForStops().get(i)> changeDto.getCumulativeMinutesFromStartForStops().get(i+1)){
                throw new IllegalArgumentException("CumulativeMinutesFromStart must be in ascending order");
            }
        }

        routeStopOffsetRepo.deleteByRoute(route);

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

        List<RouteStop> sortedStops = updatedRoute.getRouteStops().stream()
                .sorted(Comparator.comparing(RouteStop::getStopOrder))
                .toList();

        for (int i = 0; i < sortedStops.size(); i++) {
            RouteStop rs = sortedStops.get(i);
            Integer c = changeDto.getCumulativeMinutesFromStartForStops().get(i);
            RouteStopOffset offset = new RouteStopOffset(rs.getRoute(), rs.getStop(), c);
            routeStopOffsetRepo.save(offset);
        }
        return routeMapper.toDto(updatedRoute);
    }

    @Transactional
    public RouteDto updateRouteStatus(ChangeRouteStatusDto changeRouteStatusDto){
        Route existingRoute = routeRepo.findById(changeRouteStatusDto.getId()).orElseThrow(
                ()-> new RuntimeException("Route with id " + changeRouteStatusDto.getId() + " not found")
        );
        existingRoute.setStatus(changeRouteStatusDto.getNewRouteStatus());
        return routeMapper.toDto(routeRepo.save(existingRoute));
    }

    @Transactional
    public RouteDto updateRouteOffsets(ChangeRouteOffsetsDto updateDto) {
        Route route = routeRepo.findById(updateDto.getId()).orElseThrow(
                () -> new RuntimeException("Route with id " + updateDto.getId() + " not found")
        );

        for (int i = 0; i < updateDto.getCumulativeMinutesFromStartForStops().size()-1; i++) {
            if (updateDto.getCumulativeMinutesFromStartForStops().get(i)> updateDto.getCumulativeMinutesFromStartForStops().get(i+1)){
                throw new IllegalArgumentException("CumulativeMinutesFromStart must be in ascending order");
            }
        }
        List<RouteStop> sortedStops = route.getRouteStops().stream()
                .sorted(Comparator.comparing(RouteStop::getStopOrder))
                .toList();

        if (sortedStops.size() != updateDto.getCumulativeMinutesFromStartForStops().size()) {
            throw new IllegalArgumentException("Cumulative minutes list must match the number of stops in the route");
        }
        routeStopOffsetRepo.deleteByRoute(route);

        for (int i = 0; i < sortedStops.size(); i++) {
            RouteStop rs = sortedStops.get(i);
            Integer cum = updateDto.getCumulativeMinutesFromStartForStops().get(i);
            RouteStopOffset offset = new RouteStopOffset(rs.getRoute(), rs.getStop(), cum);
            routeStopOffsetRepo.save(offset);
        }
        return routeMapper.toDto(route);
    }

    public RouteDto getRouteById(Long routeId) {
        Route existingRoute = routeRepo.findById(routeId).orElseThrow(
                ()-> new RuntimeException("Route with id " + routeId + " not found")
        );
        return routeMapper.toDto(existingRoute);
    }

    public List<RouteDto> getAllRoutes(){
        List<Route> routes = routeRepo.findAll();
        return routes.stream().map(routeMapper::toDto).toList();
    }

    @Transactional
    public boolean deleteRouteById(Long routeId){
        try{
            routeRepo.findById(routeId).ifPresent(routeStopOffsetRepo::deleteByRoute);
            routeRepo.deleteById(routeId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
