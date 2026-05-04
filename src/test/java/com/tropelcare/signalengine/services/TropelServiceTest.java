package com.tropelcare.signalengine.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tropelcare.signalengine.dtos.tropel.TropelRequest;
import com.tropelcare.signalengine.exceptions.BadRequestException;
import com.tropelcare.signalengine.models.Guardian;
import com.tropelcare.signalengine.models.Sector;
import com.tropelcare.signalengine.models.enums.Climate;
import com.tropelcare.signalengine.repositories.GuardianRepository;
import com.tropelcare.signalengine.repositories.SectorRepository;
import com.tropelcare.signalengine.repositories.TropelRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TropelServiceTest {

    @Mock
    private TropelRepository tropelRepository;
    @Mock
    private SectorRepository sectorRepository;
    @Mock
    private GuardianRepository guardianRepository;

    private TropelService tropelService;

    @BeforeEach
    void setUp() {
        tropelService = new TropelService(tropelRepository, sectorRepository, guardianRepository);
    }

    @Test
    void creatingTropelInFullSectorThrowsBadRequest() {
        Sector sector = new Sector();
        sector.setId(1L);
        sector.setSectorCode("SECTOR-7");
        sector.setClimate(Climate.RETRO_ARCADE);
        sector.setCapacity(1);
        sector.setCurrentLoad(1);
        sector.setStabilityLevel(100);
        sector.setCreatedAt(Instant.now());

        Guardian guardian = new Guardian();
        guardian.setId(1L);
        guardian.setDisplayName("Cameron Walker");
        guardian.setEmail("cameron@tuckersoft.com");
        guardian.setNotificationEmail("team@example.com");
        guardian.setCreatedAt(Instant.now());

        when(tropelRepository.existsByName("BipBop")).thenReturn(false);
        when(sectorRepository.findById(1L)).thenReturn(Optional.of(sector));
        when(guardianRepository.findById(1L)).thenReturn(Optional.of(guardian));

        assertThrows(BadRequestException.class, () -> tropelService.create(new TropelRequest(
                "BipBop",
                "GLITCHY",
                1L,
                1L
        )));
        verify(tropelRepository, never()).save(any());
    }
}
