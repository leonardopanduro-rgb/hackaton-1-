package com.tropelcare.signalengine.dtos.signal;

import java.time.Instant;

public record SignalResponse(
        Long id,
        Long tropelId,
        String tropelName,
        Long guardianId,
        String guardianName,
        String senderTag,
        String rawContent,
        String signalType,
        String severity,
        String assignedUnit,
        String recommendedAction,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
