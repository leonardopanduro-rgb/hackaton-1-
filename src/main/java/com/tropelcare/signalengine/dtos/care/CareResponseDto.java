package com.tropelcare.signalengine.dtos.care;

import java.time.Instant;

public record CareResponseDto(
        Long id,
        Long signalId,
        String responseCode,
        String description,
        Instant createdAt
) {
}
