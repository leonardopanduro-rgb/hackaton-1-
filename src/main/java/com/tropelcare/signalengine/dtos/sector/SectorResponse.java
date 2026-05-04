package com.tropelcare.signalengine.dtos.sector;

import java.time.Instant;

public record SectorResponse(
        Long id,
        String sectorCode,
        String climate,
        Integer capacity,
        Integer currentLoad,
        Integer stabilityLevel,
        Instant createdAt
) {
}
