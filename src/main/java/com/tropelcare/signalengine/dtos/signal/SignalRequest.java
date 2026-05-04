package com.tropelcare.signalengine.dtos.signal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignalRequest(
        @NotNull Long tropelId,
        @NotNull Long guardianId,
        @NotBlank @Size(max = 80) String senderTag,
        @NotBlank @Size(min = 10) String rawContent
) {
}
