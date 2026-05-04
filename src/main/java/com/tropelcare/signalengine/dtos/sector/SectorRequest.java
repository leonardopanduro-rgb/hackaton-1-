package com.tropelcare.signalengine.dtos.sector;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SectorRequest(
        @NotBlank @Size(max = 80) String sectorCode,
        @NotBlank String climate,
        @NotNull @Positive Integer capacity
) {
}
