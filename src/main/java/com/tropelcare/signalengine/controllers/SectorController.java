package com.tropelcare.signalengine.controllers;

import com.tropelcare.signalengine.dtos.sector.SectorRequest;
import com.tropelcare.signalengine.dtos.sector.SectorResponse;
import com.tropelcare.signalengine.services.SectorService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sectors")
@RequiredArgsConstructor
public class SectorController {

    private final SectorService sectorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SectorResponse create(@Valid @RequestBody SectorRequest request) {
        return sectorService.create(request);
    }

    @GetMapping
    public List<SectorResponse> findAll() {
        return sectorService.findAll();
    }

    @GetMapping("/{id}")
    public SectorResponse findById(@PathVariable Long id) {
        return sectorService.findById(id);
    }
}
