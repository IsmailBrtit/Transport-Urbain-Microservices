package Transport_Urbain_Microservices.route_service.controller;

import Transport_Urbain_Microservices.route_service.dto.StopDto;
import Transport_Urbain_Microservices.route_service.service.StopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stop")
@RequiredArgsConstructor
public class StopController {

    private final StopService stopService;

    @PostMapping
    public ResponseEntity<StopDto> createStop(@RequestBody StopDto stopDto) {
        return ResponseEntity.ok(stopService.createStop(stopDto));
    }

    @PutMapping
    public ResponseEntity<StopDto> updateStop(@RequestBody StopDto stopDto) {
        return ResponseEntity.ok(stopService.updateStop(stopDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StopDto> getStopById(@PathVariable Long id) {
        return ResponseEntity.ok(stopService.getStopById(id));
    }

    @GetMapping
    public ResponseEntity<List<StopDto>> getAllStops() {
        return ResponseEntity.ok(stopService.getAllStops());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStop(@PathVariable Long id) {
        stopService.deleteStopById(id);
        return ResponseEntity.noContent().build();
    }
}