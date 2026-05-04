package com.tropelcare.signalengine.services;

import com.tropelcare.signalengine.dtos.guardian.GuardianResponse;
import com.tropelcare.signalengine.exceptions.NotFoundException;
import com.tropelcare.signalengine.mappers.DtoMapper;
import com.tropelcare.signalengine.repositories.GuardianRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GuardianService {

    private final GuardianRepository guardianRepository;

    @Transactional(readOnly = true)
    public List<GuardianResponse> findAll() {
        return guardianRepository.findAll()
                .stream()
                .map(DtoMapper::toGuardianResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GuardianResponse findById(Long id) {
        return guardianRepository.findById(id)
                .map(DtoMapper::toGuardianResponse)
                .orElseThrow(() -> new NotFoundException("No existe un guardian con id " + id));
    }
}
