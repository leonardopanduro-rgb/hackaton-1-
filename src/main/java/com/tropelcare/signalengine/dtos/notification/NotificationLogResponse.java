package com.tropelcare.signalengine.dtos.notification;

import java.time.Instant;

public record NotificationLogResponse(
        Long id,
        Long signalId,
        String recipientEmail,
        String subject,
        String notifStatus,
        String errorMessage,
        Instant sentAt,
        Instant createdAt
) {
}
