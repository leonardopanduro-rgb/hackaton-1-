package com.tropelcare.signalengine.services;

import com.tropelcare.signalengine.dtos.sector.SectorRequest;
import com.tropelcare.signalengine.dtos.sector.SectorResponse;
import com.tropelcare.signalengine.exceptions.ConflictException;
import com.tropelcare.signalengine.exceptions.NotFoundException;
import com.tropelcare.signalengine.mappers.DtoMapper;
import com.tropelcare.signalengine.models.Sector;
import com.tropelcare.signalengine.models.enums.Climate;
import com.tropelcare.signalengine.repositories.SectorRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SectorService {

    private final SectorRepository sectorRepository;

    @Transactional
    public SectorResponse create(SectorRequest request) {
        if (sectorRepository.existsBySectorCode(request.sectorCode())) {
            throw new ConflictException("Ya existe un sector con codigo " + request.sectorCode());
        }

        Sector sector = new Sector();
        sector.setSectorCode(request.sectorCode());
        sector.setClimate(EnumParser.parse(Climate.class, request.climate(), "climate"));
        sector.setCapacity(request.capacity());
        sector.setCurrentLoad(0);
        sector.setStabilityLevel(100);
        sector.setCreatedAt(Instant.now());

        return DtoMapper.toSectorResponse(sectorRepository.save(sector));
    }

    @Transactional(readOnly = true)
    public List<SectorResponse> findAll() {
        return sectorRepository.findAll()
                .stream()
                .map(DtoMapper::toSectorResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SectorResponse findById(Long id) {
        return sectorRepository.findById(id)
                .map(DtoMapper::toSectorResponse)
                .orElseThrow(() -> new NotFoundException("No existe un sector con id " + id));
    }
}
