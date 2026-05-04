package com.tropelcare.signalengine.dtos.tropel;

import java.time.Instant;

public record TropelResponse(
        Long id,
        String name,
        String species,
        String vitalState,
        Integer energyLevel,
        Integer chaosIndex,
        Integer mutationStage,
        Long sectorId,
        String sectorCode,
        Long guardianId,
        String guardianName,
        Instant createdAt,
        Instant updatedAt
) {
}
