package Transport_Urbain_Microservices.route_service.controller;

import Transport_Urbain_Microservices.route_service.dto.*;
import Transport_Urbain_Microservices.route_service.service.RouteService;
import Transport_Urbain_Microservices.route_service.service.StopService;
import lombok.RequiredArgsConstructor;
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
        return ResponseEntity.ok(routeService.createRoute(routeDto));
    }

    @PutMapping("/update/info")
    public ResponseEntity<RouteDto> updateRouteInfo(@RequestBody ChangeRouteInfoDto routeDto) {
        return ResponseEntity.ok(routeService.updateRouteInfo(routeDto));
    }

    @PutMapping("/update/stops")
    public ResponseEntity<RouteDto> updateRouteStops(@RequestBody ChangeRouteStopsDto routeDto) {
        return ResponseEntity.ok(routeService.updateRouteStops(routeDto));
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
    public ResponseEntity<Void> deleteRoute(@PathVariable Long routeId) {
        boolean deleted = routeService.deleteRouteById(routeId);
        if (!deleted) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.noContent().build();
    }
}