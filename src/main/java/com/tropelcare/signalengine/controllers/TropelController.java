package com.tropelcare.signalengine.controllers;

import com.tropelcare.signalengine.dtos.common.PageResponse;
import com.tropelcare.signalengine.dtos.tropel.TropelRequest;
import com.tropelcare.signalengine.dtos.tropel.TropelResponse;
import com.tropelcare.signalengine.services.TropelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tropels")
@RequiredArgsConstructor
public class TropelController {

    private final TropelService tropelService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TropelResponse create(@Valid @RequestBody TropelRequest request) {
        return tropelService.create(request);
    }

    @GetMapping
    public PageResponse<TropelResponse> findAll(@RequestParam(required = false) String species,
                                                @RequestParam(required = false) String vitalState,
                                                @RequestParam(required = false) Long sectorId,
                                                @RequestParam(required = false) Long guardianId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return tropelService.findAll(species, vitalState, sectorId, guardianId, page, size);
    }

    @GetMapping("/{id}")
    public TropelResponse findById(@PathVariable Long id) {
        return tropelService.findById(id);
    }
}
