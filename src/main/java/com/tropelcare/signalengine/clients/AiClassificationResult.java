package com.tropelcare.signalengine.clients;

import com.tropelcare.signalengine.models.enums.Severity;
import com.tropelcare.signalengine.models.enums.SignalType;

public record AiClassificationResult(
        SignalType signalType,
        Severity severity,
        String assignedUnit,
        String recommendedAction
) {

    public static AiClassificationResult fallback() {
        return new AiClassificationResult(
                SignalType.SENAL_CORRUPTA,
                Severity.LEVE,
                SignalType.SENAL_CORRUPTA.getAssignedUnit(),
                "Archivar la señal y revisar manualmente si se repite."
        );
    }
}
