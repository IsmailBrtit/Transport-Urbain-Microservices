package Transport_Urbain_Microservices.route_service.controller;

import Transport_Urbain_Microservices.route_service.dto.CreateRunDTO;
import Transport_Urbain_Microservices.route_service.dto.RunDetailsDto;
import Transport_Urbain_Microservices.route_service.mapper.RunMapper;
import Transport_Urbain_Microservices.route_service.service.RunService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/run")
public class RunController {

    private final RunService runService;

    @PostMapping("/create")
    public ResponseEntity<RunDetailsDto> createRun(@RequestBody CreateRunDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(runService.createRun(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RunDetailsDto> getRunById(@PathVariable Long id) {
        return ResponseEntity.ok(runService.getRunById(id));
    }

    @GetMapping
    public ResponseEntity<List<RunDetailsDto>> getAllRuns() {
        return ResponseEntity.ok(runService.getAllRuns());
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<RunDetailsDto>> getAllRunsForRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(runService.getAllRunsForRoute(routeId));
    }

    @GetMapping("/route/{routeId}/day/{givenDay}")
    public ResponseEntity<List<RunDetailsDto>> getAllRunsForRouteForGivenDay(
            @PathVariable Long routeId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate givenDay) {
        return ResponseEntity.ok(runService.getAllRunsForRouteForGivenDay(routeId, givenDay));
    }

    @GetMapping("/stop/{stopId}")
    public ResponseEntity<List<RunDetailsDto>> getAllRunsForStop(@PathVariable Long stopId) {
        return ResponseEntity.ok(runService.getAllRunsForStop(stopId));
    }

    @GetMapping("/stop/{stopId}/day/{givenDay}")
    public ResponseEntity<List<RunDetailsDto>> getAllRunsForStopForGivenDay(
            @PathVariable Long stopId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate givenDay) {
        return ResponseEntity.ok(runService.getAllRunsForStopForGivenDay(stopId, givenDay));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteRun(@PathVariable Long id) {
        runService.deleteRunById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/special/{date}")
    public ResponseEntity<Void> deleteAllSpecialRunsForGivenDay(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        runService.deleteAllSpecialRunsForGivenDay(date);
        return ResponseEntity.noContent().build();
    }


}
