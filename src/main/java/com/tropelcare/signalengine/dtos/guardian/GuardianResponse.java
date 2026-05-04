package com.tropelcare.signalengine.dtos.guardian;

import java.time.Instant;

public record GuardianResponse(
        Long id,
        String displayName,
        String email,
        String notificationEmail,
        Instant createdAt
) {
}
