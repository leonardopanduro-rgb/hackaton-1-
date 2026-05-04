package com.tropelcare.signalengine.models.enums;

public enum SignalType {
    HAMBRE("Laboratorio de Nutricion", ResponseCode.DISPATCH_NUTRIENT_PACK),
    ABANDONO("Unidad de Bienestar", ResponseCode.SEND_COMPANIONSHIP_PROTOCOL),
    MUTACION("Division Genetica", ResponseCode.ISOLATE_AND_OBSERVE),
    FUGA("Equipo de Contencion", ResponseCode.ACTIVATE_SECTOR_LOCK),
    CONFLICTO("Consejo de Mediacion", ResponseCode.DEPLOY_MEDIATION_FIELD),
    REPRODUCCION_MASIVA("Control Demografico", ResponseCode.ENABLE_POPULATION_CONTROL),
    SENAL_CORRUPTA("Archivo de Senales", ResponseCode.ARCHIVE_AND_IGNORE);

    private final String assignedUnit;
    private final ResponseCode responseCode;

    SignalType(String assignedUnit, ResponseCode responseCode) {
        this.assignedUnit = assignedUnit;
        this.responseCode = responseCode;
    }

    public String getAssignedUnit() {
        return assignedUnit;
    }

    public ResponseCode getResponseCode() {
        return responseCode;
    }
}
