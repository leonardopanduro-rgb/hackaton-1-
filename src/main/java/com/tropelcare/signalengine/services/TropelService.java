package com.tropelcare.signalengine.services;

import com.tropelcare.signalengine.dtos.common.PageResponse;
import com.tropelcare.signalengine.dtos.tropel.TropelRequest;
import com.tropelcare.signalengine.dtos.tropel.TropelResponse;
import com.tropelcare.signalengine.exceptions.BadRequestException;
import com.tropelcare.signalengine.exceptions.ConflictException;
import com.tropelcare.signalengine.exceptions.NotFoundException;
import com.tropelcare.signalengine.mappers.DtoMapper;
import com.tropelcare.signalengine.models.Guardian;
import com.tropelcare.signalengine.models.Sector;
import com.tropelcare.signalengine.models.Tropel;
import com.tropelcare.signalengine.models.enums.Species;
import com.tropelcare.signalengine.models.enums.VitalState;
import com.tropelcare.signalengine.repositories.GuardianRepository;
import com.tropelcare.signalengine.repositories.SectorRepository;
import com.tropelcare.signalengine.repositories.TropelRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TropelService {

    private final TropelRepository tropelRepository;
    private final SectorRepository sectorRepository;
    private final GuardianRepository guardianRepository;

    @Transactional
    public TropelResponse create(TropelRequest request) {
        if (tropelRepository.existsByName(request.name())) {
            throw new ConflictException("Ya existe un Tropel con nombre " + request.name());
        }

        Sector sector = sectorRepository.findById(request.sectorId())
                .orElseThrow(() -> new NotFoundException("No existe un sector con id " + request.sectorId()));
        Guardian guardian = guardianRepository.findById(request.guardianId())
                .orElseThrow(() -> new NotFoundException("No existe un guardian con id " + request.guardianId()));

        if (sector.getCurrentLoad() >= sector.getCapacity()) {
            throw new BadRequestException("El sector esta lleno");
        }

        Instant now = Instant.now();
        Tropel tropel = new Tropel();
        tropel.setName(request.name());
        tropel.setSpecies(EnumParser.parse(Species.class, request.species(), "species"));
        tropel.setVitalState(VitalState.ESTABLE);
        tropel.setEnergyLevel(80);
        tropel.setChaosIndex(10);
        tropel.setMutationStage(0);
        tropel.setSector(sector);
        tropel.setGuardian(guardian);
        tropel.setCreatedAt(now);
        tropel.setUpdatedAt(now);

        sector.setCurrentLoad(sector.getCurrentLoad() + 1);
        sectorRepository.save(sector);

        return DtoMapper.toTropelResponse(tropelRepository.save(tropel));
    }

    @Transactional(readOnly = true)
    public PageResponse<TropelResponse> findAll(String species,
                                                String vitalState,
                                                Long sectorId,
                                                Long guardianId,
                                                int page,
                                                int size) {
        Specification<Tropel> spec = Specification.where(null);

        if (species != null && !species.isBlank()) {
            Species parsedSpecies = EnumParser.parse(Species.class, species, "species");
            spec = spec.and((root, query, cb) -> cb.equal(root.get("species"), parsedSpecies));
        }
        if (vitalState != null && !vitalState.isBlank()) {
            VitalState parsedState = EnumParser.parse(VitalState.class, vitalState, "vitalState");
            spec = spec.and((root, query, cb) -> cb.equal(root.get("vitalState"), parsedState));
        }
        if (sectorId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("sector").get("id"), sectorId));
        }
        if (guardianId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("guardian").get("id"), guardianId));
        }

        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return PageResponse.from(tropelRepository.findAll(spec, pageable), DtoMapper::toTropelResponse);
    }

    @Transactional(readOnly = true)
    public TropelResponse findById(Long id) {
        return tropelRepository.findById(id)
                .map(DtoMapper::toTropelResponse)
                .orElseThrow(() -> new NotFoundException("No existe un Tropel con id " + id));
    }
}
