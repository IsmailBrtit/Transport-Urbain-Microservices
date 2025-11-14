package com.example.abonnements_service.controller;

import com.example.abonnements_service.dto.ForfaitRequest;
import com.example.abonnements_service.dto.ForfaitResponse;
import com.example.abonnements_service.service.ForfaitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/forfaits")
@RequiredArgsConstructor
public class ForfaitController {

    private final ForfaitService forfaitService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ForfaitResponse createForfait(@Valid @RequestBody ForfaitRequest forfaitRequest) {
        return forfaitService.createForfait(forfaitRequest);
    }

    @GetMapping("/{id}")
    public ForfaitResponse getForfaitById(@PathVariable UUID id) {
        return forfaitService.findById(id)
                .orElseThrow(() -> new ForfaitService.ResourceNotFoundException("Forfait not found with id: " + id));
    }

    @GetMapping
    public List<ForfaitResponse> getAllForfaits(@RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        if (activeOnly) {
            return forfaitService.findAllActive();
        }
        return forfaitService.findAll();
    }

    @PutMapping("/{id}")
    public ForfaitResponse updateForfait(@PathVariable UUID id, @Valid @RequestBody ForfaitRequest forfaitRequest) {
        return forfaitService.updateForfait(id, forfaitRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteForfait(@PathVariable UUID id) {
        forfaitService.deleteForfait(id);
    }
}
