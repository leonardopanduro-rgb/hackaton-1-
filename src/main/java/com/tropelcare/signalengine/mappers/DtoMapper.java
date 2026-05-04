package com.tropelcare.signalengine.mappers;

import com.tropelcare.signalengine.dtos.care.CareResponseDto;
import com.tropelcare.signalengine.dtos.guardian.GuardianResponse;
import com.tropelcare.signalengine.dtos.notification.NotificationLogResponse;
import com.tropelcare.signalengine.dtos.sector.SectorResponse;
import com.tropelcare.signalengine.dtos.signal.SignalResponse;
import com.tropelcare.signalengine.dtos.tropel.TropelResponse;
import com.tropelcare.signalengine.models.CareResponse;
import com.tropelcare.signalengine.models.Guardian;
import com.tropelcare.signalengine.models.NotificationLog;
import com.tropelcare.signalengine.models.Sector;
import com.tropelcare.signalengine.models.Tropel;
import com.tropelcare.signalengine.models.TropelSignal;

public final class DtoMapper {

    private DtoMapper() {
    }

    public static GuardianResponse toGuardianResponse(Guardian guardian) {
        return new GuardianResponse(
                guardian.getId(),
                guardian.getDisplayName(),
                guardian.getEmail(),
                guardian.getNotificationEmail(),
                guardian.getCreatedAt()
        );
    }

    public static SectorResponse toSectorResponse(Sector sector) {
        return new SectorResponse(
                sector.getId(),
                sector.getSectorCode(),
                sector.getClimate().name(),
                sector.getCapacity(),
                sector.getCurrentLoad(),
                sector.getStabilityLevel(),
                sector.getCreatedAt()
        );
    }

    public static TropelResponse toTropelResponse(Tropel tropel) {
        return new TropelResponse(
                tropel.getId(),
                tropel.getName(),
                tropel.getSpecies().name(),
                tropel.getVitalState().name(),
                tropel.getEnergyLevel(),
                tropel.getChaosIndex(),
                tropel.getMutationStage(),
                tropel.getSector().getId(),
                tropel.getSector().getSectorCode(),
                tropel.getGuardian().getId(),
                tropel.getGuardian().getDisplayName(),
                tropel.getCreatedAt(),
                tropel.getUpdatedAt()
        );
    }

    public static SignalResponse toSignalResponse(TropelSignal signal) {
        return new SignalResponse(
                signal.getId(),
                signal.getTropel().getId(),
                signal.getTropel().getName(),
                signal.getGuardian().getId(),
                signal.getGuardian().getDisplayName(),
                signal.getSenderTag(),
                signal.getRawContent(),
                signal.getSignalType().name(),
                signal.getSeverity().name(),
                signal.getAssignedUnit(),
                signal.getRecommendedAction(),
                signal.getStatus().name(),
                signal.getCreatedAt(),
                signal.getUpdatedAt()
        );
    }

    public static CareResponseDto toCareResponseDto(CareResponse careResponse) {
        return new CareResponseDto(
                careResponse.getId(),
                careResponse.getSignal().getId(),
                careResponse.getResponseCode().name(),
                careResponse.getDescription(),
                careResponse.getCreatedAt()
        );
    }

    public static NotificationLogResponse toNotificationLogResponse(NotificationLog log) {
        return new NotificationLogResponse(
                log.getId(),
                log.getSignal().getId(),
                log.getRecipientEmail(),
                log.getSubject(),
                log.getNotifStatus().name(),
                log.getErrorMessage(),
                log.getSentAt(),
                log.getCreatedAt()
        );
    }
}
