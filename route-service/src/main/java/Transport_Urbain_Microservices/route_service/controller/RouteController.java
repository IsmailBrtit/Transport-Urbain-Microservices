package Transport_Urbain_Microservices.route_service.controller;

import Transport_Urbain_Microservices.route_service.dto.*;
import Transport_Urbain_Microservices.route_service.service.RouteService;
import Transport_Urbain_Microservices.route_service.service.StopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/route")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PostMapping
    public ResponseEntity<RouteDto> createRoute(@RequestBody RouteDto routeDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.createRoute(routeDto));
    }

    @PutMapping("/update/info")
    public ResponseEntity<RouteDto> updateRouteInfo(@RequestBody ChangeRouteInfoDto routeDto) {
        return ResponseEntity.ok(routeService.updateRouteInfo(routeDto));
    }

    @PutMapping("/update/stops")
    public ResponseEntity<RouteDto> updateRouteStops(@RequestBody ChangeRouteStopsDto routeDto) {
        return ResponseEntity.ok(routeService.updateRouteStops(routeDto));
    }

    @PutMapping("/update/offsets")
    public ResponseEntity<RouteDto> updateRouteOffsets(@RequestBody ChangeRouteOffsetsDto changeRouteOffsetsDto) {
        return ResponseEntity.ok(routeService.updateRouteOffsets(changeRouteOffsetsDto));
    }

    @PutMapping("/update/status")
    public ResponseEntity<RouteDto> updateRouteStatus(@RequestBody ChangeRouteStatusDto statusDto) {
        return ResponseEntity.ok(routeService.updateRouteStatus(statusDto));
    }

    @GetMapping("/{routeId}")
    public ResponseEntity<RouteDto> getRouteById(@PathVariable Long routeId) {
        return ResponseEntity.ok(routeService.getRouteById(routeId));
    }

    @GetMapping
    public ResponseEntity<List<RouteDto>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    @DeleteMapping("/{routeId}")
    public ResponseEntity<Boolean> deleteRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(routeService.deleteRouteById(routeId));
    }
}