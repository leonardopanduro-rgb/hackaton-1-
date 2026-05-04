package com.tropelcare.signalengine.controllers;

import com.tropelcare.signalengine.dtos.care.CareResponseDto;
import com.tropelcare.signalengine.dtos.common.PageResponse;
import com.tropelcare.signalengine.dtos.notification.NotificationLogResponse;
import com.tropelcare.signalengine.dtos.signal.SignalRequest;
import com.tropelcare.signalengine.dtos.signal.SignalResponse;
import com.tropelcare.signalengine.services.SignalService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/v1/signals")
@RequiredArgsConstructor
public class SignalController {

    private final SignalService signalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SignalResponse create(@Valid @RequestBody SignalRequest request) {
        return signalService.create(request);
    }

    @GetMapping
    public PageResponse<SignalResponse> findAll(@RequestParam(required = false) String signalType,
                                                @RequestParam(required = false) String severity,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) Long tropelId,
                                                @RequestParam(required = false) Long guardianId,
                                                @RequestParam(required = false)
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                @RequestParam(required = false)
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return signalService.findAll(signalType, severity, status, tropelId, guardianId, from, to, page, size);
    }

    @GetMapping("/{id}")
    public SignalResponse findById(@PathVariable Long id) {
        return signalService.findById(id);
    }

    @GetMapping("/{id}/care-response")
    public CareResponseDto findCareResponse(@PathVariable Long id) {
        return signalService.findCareResponse(id);
    }

    @GetMapping("/{id}/notifications")
    public List<NotificationLogResponse> findNotifications(@PathVariable Long id) {
        return signalService.findNotifications(id);
    }
}
