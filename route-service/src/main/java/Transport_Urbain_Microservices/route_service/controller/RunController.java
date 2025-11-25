//package Transport_Urbain_Microservices.route_service.controller;
//
//import Transport_Urbain_Microservices.route_service.dto.CreateRunDTO;
//import Transport_Urbain_Microservices.route_service.dto.RunDetailsDto;
//import Transport_Urbain_Microservices.route_service.dto.UpcomingRunForStopDto;
//import Transport_Urbain_Microservices.route_service.mapper.RunMapper;
//import Transport_Urbain_Microservices.route_service.service.RunService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/run")
//public class RunController {
//
//    private final RunService runService;
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @PostMapping("/create")
//    public ResponseEntity<RunDetailsDto> createRun(@RequestBody CreateRunDTO dto) {
//        return ResponseEntity.ok(RunMapper.mapToRunDetailsDto(runService.createRun(dto)));
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<RunDetailsDto> getRunById(@PathVariable Long id) {
//        return ResponseEntity.ok(runService.getRunById(id));
//    }
//
//    @DeleteMapping("/delete/{id}")
//    public ResponseEntity<Void> deleteRun(@PathVariable Long id) {
//        runService.deleteRunById(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    @GetMapping
//    public ResponseEntity<List<RunDetailsDto>> getAllRuns() {
//        return ResponseEntity.ok(runService.getAllRuns());
//    }
//
//    @GetMapping("/next24h")
//    public ResponseEntity<List<RunDetailsDto>> getRunsNext24h() {
//        return ResponseEntity.ok(runService.getRunsForRouteInNext24Hours(null));
//    }
//
//    @GetMapping("/route/{routeId}")
//    public ResponseEntity<List<RunDetailsDto>> getRunsByRoute(@PathVariable Long routeId) {
//        return ResponseEntity.ok(runService.getAllRunsByRoute(routeId));
//    }
//
//    @GetMapping("route/{routeId}/next24h")
//    public ResponseEntity<List<RunDetailsDto>> getRunsForRouteNext24h(@PathVariable Long routeId) {
//        return ResponseEntity.ok(runService.getRunsForRouteInNext24Hours(routeId));
//    }
//
//    @GetMapping("/stop/{stopId}")
//    public ResponseEntity<List<RunDetailsDto>> getRunsByStop(@PathVariable Long stopId) {
//        return ResponseEntity.ok(runService.getAllRunsByStop(stopId));
//    }
//
//    @GetMapping("/stop/{stopId}/next24h")
//    public ResponseEntity<List<UpcomingRunForStopDto>> getRunsForStopNext24h(@PathVariable Long stopId) {
//        return ResponseEntity.ok(runService.getRunsForStopInNext24Hours(stopId));
//    }
//
//}
