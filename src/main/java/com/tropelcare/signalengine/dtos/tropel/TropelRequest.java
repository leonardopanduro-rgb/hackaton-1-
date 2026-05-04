package com.tropelcare.signalengine.dtos.tropel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TropelRequest(
        @NotBlank @Size(min = 2, max = 40) String name,
        @NotBlank String species,
        @NotNull Long sectorId,
        @NotNull Long guardianId
) {
}
