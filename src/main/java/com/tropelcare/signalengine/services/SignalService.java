package com.tropelcare.signalengine.services;

import com.tropelcare.signalengine.clients.AiClassificationClient;
import com.tropelcare.signalengine.clients.AiClassificationResult;
import com.tropelcare.signalengine.dtos.care.CareResponseDto;
import com.tropelcare.signalengine.dtos.common.PageResponse;
import com.tropelcare.signalengine.dtos.notification.NotificationLogResponse;
import com.tropelcare.signalengine.dtos.signal.SignalRequest;
import com.tropelcare.signalengine.dtos.signal.SignalResponse;
import com.tropelcare.signalengine.events.TropelSignalCreatedEvent;
import com.tropelcare.signalengine.exceptions.BadRequestException;
import com.tropelcare.signalengine.exceptions.NotFoundException;
import com.tropelcare.signalengine.mappers.DtoMapper;
import com.tropelcare.signalengine.models.CareResponse;
import com.tropelcare.signalengine.models.Guardian;
import com.tropelcare.signalengine.models.Sector;
import com.tropelcare.signalengine.models.Tropel;
import com.tropelcare.signalengine.models.TropelSignal;
import com.tropelcare.signalengine.models.enums.Severity;
import com.tropelcare.signalengine.models.enums.SignalStatus;
import com.tropelcare.signalengine.models.enums.SignalType;
import com.tropelcare.signalengine.models.enums.VitalState;
import com.tropelcare.signalengine.repositories.CareResponseRepository;
import com.tropelcare.signalengine.repositories.GuardianRepository;
import com.tropelcare.signalengine.repositories.NotificationLogRepository;
import com.tropelcare.signalengine.repositories.SectorRepository;
import com.tropelcare.signalengine.repositories.TropelRepository;
import com.tropelcare.signalengine.repositories.TropelSignalRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignalService {

    private final TropelSignalRepository signalRepository;
    private final TropelRepository tropelRepository;
    private final GuardianRepository guardianRepository;
    private final SectorRepository sectorRepository;
    private final CareResponseRepository careResponseRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final AiClassificationClient aiClassificationClient;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public SignalResponse create(SignalRequest request) {
        Tropel tropel = tropelRepository.findById(request.tropelId())
                .orElseThrow(() -> new NotFoundException("No existe un Tropel con id " + request.tropelId()));

        if (!tropel.getGuardian().getId().equals(request.guardianId())) {
            throw new BadRequestException("El guardianId no corresponde al guardian responsable de este Tropel");
        }

        Guardian guardian = guardianRepository.findById(request.guardianId())
                .orElseThrow(() -> new NotFoundException("No existe un guardian con id " + request.guardianId()));

        boolean successfulClassification = true;
        AiClassificationResult classification;
        try {
            classification = aiClassificationClient.classify(request.rawContent());
        } catch (Exception exception) {
            successfulClassification = false;
            classification = AiClassificationResult.fallback();
        }

        if (successfulClassification) {
            updateTropelStats(tropel, classification.severity());
            updateSectorStability(tropel.getSector(), classification.signalType());
            tropelRepository.save(tropel);
            sectorRepository.save(tropel.getSector());
        }

        Instant now = Instant.now();
        TropelSignal signal = new TropelSignal();
        signal.setTropel(tropel);
        signal.setGuardian(guardian);
        signal.setSenderTag(request.senderTag());
        signal.setRawContent(request.rawContent());
        signal.setSignalType(classification.signalType());
        signal.setSeverity(classification.severity());
        signal.setAssignedUnit(classification.assignedUnit());
        signal.setRecommendedAction(classification.recommendedAction());
        signal.setStatus(successfulClassification ? SignalStatus.RECIBIDA : SignalStatus.ERROR);
        signal.setCreatedAt(now);
        signal.setUpdatedAt(now);

        TropelSignal savedSignal = signalRepository.save(signal);
        createCareResponse(savedSignal);

        if (successfulClassification) {
            eventPublisher.publishEvent(new TropelSignalCreatedEvent(savedSignal.getId()));
        }

        return DtoMapper.toSignalResponse(savedSignal);
    }

    @Transactional(readOnly = true)
    public PageResponse<SignalResponse> findAll(String signalType,
                                                String severity,
                                                String status,
                                                Long tropelId,
                                                Long guardianId,
                                                LocalDate from,
                                                LocalDate to,
                                                int page,
                                                int size) {
        Specification<TropelSignal> spec = Specification.where(null);

        if (signalType != null && !signalType.isBlank()) {
            SignalType parsedType = EnumParser.parse(SignalType.class, signalType, "signalType");
            spec = spec.and((root, query, cb) -> cb.equal(root.get("signalType"), parsedType));
        }
        if (severity != null && !severity.isBlank()) {
            Severity parsedSeverity = EnumParser.parse(Severity.class, severity, "severity");
            spec = spec.and((root, query, cb) -> cb.equal(root.get("severity"), parsedSeverity));
        }
        if (status != null && !status.isBlank()) {
            SignalStatus parsedStatus = EnumParser.parse(SignalStatus.class, status, "status");
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), parsedStatus));
        }
        if (tropelId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tropel").get("id"), tropelId));
        }
        if (guardianId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("guardian").get("id"), guardianId));
        }
        if (from != null) {
            Instant start = from.atStartOfDay().toInstant(ZoneOffset.UTC);
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), start));
        }
        if (to != null) {
            Instant endExclusive = to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("createdAt"), endExclusive));
        }

        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return PageResponse.from(signalRepository.findAll(spec, pageable), DtoMapper::toSignalResponse);
    }

    @Transactional(readOnly = true)
    public SignalResponse findById(Long id) {
        return signalRepository.findById(id)
                .map(DtoMapper::toSignalResponse)
                .orElseThrow(() -> new NotFoundException("No existe una senal con id " + id));
    }

    @Transactional(readOnly = true)
    public CareResponseDto findCareResponse(Long signalId) {
        return careResponseRepository.findBySignalId(signalId)
                .map(DtoMapper::toCareResponseDto)
                .orElseThrow(() -> new NotFoundException("No existe una respuesta de cuidado para la senal #" + signalId));
    }

    @Transactional(readOnly = true)
    public java.util.List<NotificationLogResponse> findNotifications(Long signalId) {
        if (!signalRepository.existsById(signalId)) {
            throw new NotFoundException("No existe una senal con id " + signalId);
        }
        return notificationLogRepository.findBySignalIdOrderByCreatedAtDesc(signalId)
                .stream()
                .map(DtoMapper::toNotificationLogResponse)
                .toList();
    }

    private void createCareResponse(TropelSignal signal) {
        CareResponse careResponse = new CareResponse();
        careResponse.setSignal(signal);
        careResponse.setResponseCode(signal.getSignalType().getResponseCode());
        careResponse.setDescription(signal.getRecommendedAction());
        careResponse.setCreatedAt(Instant.now());
        careResponseRepository.save(careResponse);
        signal.setCareResponse(careResponse);
    }

    private void updateTropelStats(Tropel tropel, Severity severity) {
        switch (severity) {
            case LEVE -> {
                tropel.setEnergyLevel(clamp(tropel.getEnergyLevel() - 5, 0, 100));
                tropel.setChaosIndex(clamp(tropel.getChaosIndex() + 5, 0, 100));
            }
            case MODERADO -> {
                tropel.setEnergyLevel(clamp(tropel.getEnergyLevel() - 10, 0, 100));
                tropel.setChaosIndex(clamp(tropel.getChaosIndex() + 15, 0, 100));
            }
            case GRAVE -> {
                tropel.setEnergyLevel(clamp(tropel.getEnergyLevel() - 20, 0, 100));
                tropel.setChaosIndex(clamp(tropel.getChaosIndex() + 30, 0, 100));
            }
            case CRITICO -> {
                tropel.setEnergyLevel(clamp(tropel.getEnergyLevel() - 30, 0, 100));
                tropel.setChaosIndex(clamp(tropel.getChaosIndex() + 45, 0, 100));
                tropel.setMutationStage(clamp(tropel.getMutationStage() + 1, 0, 5));
            }
        }

        if (tropel.getChaosIndex() >= 80) {
            tropel.setVitalState(VitalState.CRITICO);
        } else if (tropel.getEnergyLevel() <= 20) {
            tropel.setVitalState(VitalState.HAMBRIENTO);
        } else if (severity == Severity.CRITICO) {
            tropel.setVitalState(VitalState.MUTANDO);
        } else if (severity == Severity.GRAVE) {
            tropel.setVitalState(VitalState.AGITADO);
        }

        tropel.setUpdatedAt(Instant.now());
    }

    private void updateSectorStability(Sector sector, SignalType signalType) {
        if (signalType == SignalType.FUGA) {
            sector.setStabilityLevel(clamp(sector.getStabilityLevel() - 10, 0, 100));
        } else if (signalType == SignalType.REPRODUCCION_MASIVA) {
            sector.setStabilityLevel(clamp(sector.getStabilityLevel() - 15, 0, 100));
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
