package com.tropelcare.signalengine.controllers;

import com.tropelcare.signalengine.dtos.guardian.GuardianResponse;
import com.tropelcare.signalengine.services.GuardianService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/guardians")
@RequiredArgsConstructor
public class GuardianController {

    private final GuardianService guardianService;

    @GetMapping
    public List<GuardianResponse> findAll() {
        return guardianService.findAll();
    }

    @GetMapping("/{id}")
    public GuardianResponse findById(@PathVariable Long id) {
        return guardianService.findById(id);
    }
}
