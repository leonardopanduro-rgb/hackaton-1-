package com.tropelcare.signalengine.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tropelcare.signalengine.clients.AiClassificationClient;
import com.tropelcare.signalengine.clients.AiClassificationParser;
import com.tropelcare.signalengine.clients.AiClassificationResult;
import com.tropelcare.signalengine.dtos.signal.SignalRequest;
import com.tropelcare.signalengine.dtos.signal.SignalResponse;
import com.tropelcare.signalengine.events.TropelSignalCreatedEvent;
import com.tropelcare.signalengine.exceptions.BadRequestException;
import com.tropelcare.signalengine.models.CareResponse;
import com.tropelcare.signalengine.models.Guardian;
import com.tropelcare.signalengine.models.Sector;
import com.tropelcare.signalengine.models.Tropel;
import com.tropelcare.signalengine.models.TropelSignal;
import com.tropelcare.signalengine.models.enums.Climate;
import com.tropelcare.signalengine.models.enums.ResponseCode;
import com.tropelcare.signalengine.models.enums.Severity;
import com.tropelcare.signalengine.models.enums.SignalType;
import com.tropelcare.signalengine.models.enums.Species;
import com.tropelcare.signalengine.models.enums.VitalState;
import com.tropelcare.signalengine.repositories.CareResponseRepository;
import com.tropelcare.signalengine.repositories.GuardianRepository;
import com.tropelcare.signalengine.repositories.NotificationLogRepository;
import com.tropelcare.signalengine.repositories.SectorRepository;
import com.tropelcare.signalengine.repositories.TropelRepository;
import com.tropelcare.signalengine.repositories.TropelSignalRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class SignalServiceTest {

    @Mock
    private TropelSignalRepository signalRepository;
    @Mock
    private TropelRepository tropelRepository;
    @Mock
    private GuardianRepository guardianRepository;
    @Mock
    private SectorRepository sectorRepository;
    @Mock
    private CareResponseRepository careResponseRepository;
    @Mock
    private NotificationLogRepository notificationLogRepository;
    @Mock
    private AiClassificationClient aiClassificationClient;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private SignalService signalService;
    private Guardian guardian;
    private Sector sector;
    private Tropel tropel;

    @BeforeEach
    void setUp() {
        signalService = new SignalService(
                signalRepository,
                tropelRepository,
                guardianRepository,
                sectorRepository,
                careResponseRepository,
                notificationLogRepository,
                aiClassificationClient,
                eventPublisher
        );

        guardian = new Guardian();
        guardian.setId(1L);
        guardian.setDisplayName("Cameron Walker");
        guardian.setEmail("cameron@tuckersoft.com");
        guardian.setNotificationEmail("team@example.com");
        guardian.setCreatedAt(Instant.now());

        sector = new Sector();
        sector.setId(1L);
        sector.setSectorCode("SECTOR-7");
        sector.setClimate(Climate.RETRO_ARCADE);
        sector.setCapacity(3);
        sector.setCurrentLoad(1);
        sector.setStabilityLevel(100);
        sector.setCreatedAt(Instant.now());

        tropel = new Tropel();
        tropel.setId(1L);
        tropel.setName("BipBop");
        tropel.setSpecies(Species.GLITCHY);
        tropel.setVitalState(VitalState.ESTABLE);
        tropel.setEnergyLevel(80);
        tropel.setChaosIndex(10);
        tropel.setMutationStage(0);
        tropel.setSector(sector);
        tropel.setGuardian(guardian);
        tropel.setCreatedAt(Instant.now());
        tropel.setUpdatedAt(Instant.now());

        lenient().when(signalRepository.save(any(TropelSignal.class))).thenAnswer(invocation -> {
            TropelSignal signal = invocation.getArgument(0);
            if (signal.getId() == null) {
                signal.setId(5L);
            }
            return signal;
        });
        lenient().when(careResponseRepository.save(any(CareResponse.class))).thenAnswer(invocation -> {
            CareResponse response = invocation.getArgument(0);
            response.setId(9L);
            return response;
        });
        lenient().when(tropelRepository.save(any(Tropel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(sectorRepository.save(any(Sector.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsSignalWithValidAiClassification() {
        mockExistingTropelAndGuardian();
        when(aiClassificationClient.classify(any())).thenReturn(new AiClassificationResult(
                SignalType.HAMBRE,
                Severity.MODERADO,
                SignalType.HAMBRE.getAssignedUnit(),
                "Enviar paquete de nutrientes."
        ));

        SignalResponse response = signalService.create(validRequest(1L));

        assertEquals("HAMBRE", response.signalType());
        assertEquals("MODERADO", response.severity());
        assertEquals("Laboratorio de Nutricion", response.assignedUnit());
        assertEquals("RECIBIDA", response.status());
        verify(eventPublisher).publishEvent(any(TropelSignalCreatedEvent.class));
    }

    @Test
    void appliesFallbackWhenAiThrowsAndDoesNotPropagateException() {
        mockExistingTropelAndGuardian();
        when(aiClassificationClient.classify(any())).thenThrow(new RuntimeException("timeout"));

        SignalResponse response = signalService.create(validRequest(1L));

        assertEquals("SENAL_CORRUPTA", response.signalType());
        assertEquals("LEVE", response.severity());
        assertEquals("Archivo de Senales", response.assignedUnit());
        assertEquals("ERROR", response.status());
        assertEquals(80, tropel.getEnergyLevel());
        assertEquals(10, tropel.getChaosIndex());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void createsSignalWhenAiContentHasExtraTextAroundJson() {
        mockExistingTropelAndGuardian();
        AiClassificationParser parser = new AiClassificationParser(new ObjectMapper());
        String modelContent = """
                texto inicial
                {"signalType":"MUTACION","severity":"GRAVE","assignedUnit":"Division Genetica","recommendedAction":"Aislar al Tropel y observar la mutacion."}
                texto final
                """;
        when(aiClassificationClient.classify(any())).thenReturn(parser.parse(modelContent));

        SignalResponse response = signalService.create(validRequest(1L));

        assertEquals("MUTACION", response.signalType());
        assertEquals("GRAVE", response.severity());
        assertEquals("Division Genetica", response.assignedUnit());
        assertEquals("RECIBIDA", response.status());
    }

    @Test
    void criticalSeverityUpdatesStatsWithoutExceedingLimits() {
        mockExistingTropelAndGuardian();
        tropel.setEnergyLevel(25);
        tropel.setChaosIndex(70);
        tropel.setMutationStage(5);
        when(aiClassificationClient.classify(any())).thenReturn(new AiClassificationResult(
                SignalType.MUTACION,
                Severity.CRITICO,
                SignalType.MUTACION.getAssignedUnit(),
                "Aislar y observar."
        ));

        signalService.create(validRequest(1L));

        assertEquals(0, tropel.getEnergyLevel());
        assertEquals(100, tropel.getChaosIndex());
        assertEquals(5, tropel.getMutationStage());
        assertEquals(VitalState.CRITICO, tropel.getVitalState());
    }

    @Test
    void createsCareResponseWithMappedResponseCode() {
        mockExistingTropelAndGuardian();
        when(aiClassificationClient.classify(any())).thenReturn(new AiClassificationResult(
                SignalType.MUTACION,
                Severity.GRAVE,
                SignalType.MUTACION.getAssignedUnit(),
                "Aislar y monitorear."
        ));
        ArgumentCaptor<CareResponse> captor = ArgumentCaptor.forClass(CareResponse.class);

        signalService.create(validRequest(1L));

        verify(careResponseRepository).save(captor.capture());
        assertEquals(ResponseCode.ISOLATE_AND_OBSERVE, captor.getValue().getResponseCode());
        assertEquals("Aislar y monitorear.", captor.getValue().getDescription());
    }

    @Test
    void fugaSignalReducesSectorStabilityByTen() {
        mockExistingTropelAndGuardian();
        when(aiClassificationClient.classify(any())).thenReturn(new AiClassificationResult(
                SignalType.FUGA,
                Severity.GRAVE,
                SignalType.FUGA.getAssignedUnit(),
                "Activar cierre del sector."
        ));

        signalService.create(validRequest(1L));

        assertEquals(90, sector.getStabilityLevel());
    }

    @Test
    void guardianMismatchThrowsBadRequestBeforeCallingAi() {
        when(tropelRepository.findById(1L)).thenReturn(Optional.of(tropel));

        assertThrows(BadRequestException.class, () -> signalService.create(validRequest(99L)));
        verify(guardianRepository, never()).findById(99L);
        verify(aiClassificationClient, never()).classify(any());
        verify(signalRepository, never()).save(any());
    }

    @Test
    void successfulClassificationPublishesExactlyOneEvent() {
        mockExistingTropelAndGuardian();
        when(aiClassificationClient.classify(any())).thenReturn(new AiClassificationResult(
                SignalType.ABANDONO,
                Severity.LEVE,
                SignalType.ABANDONO.getAssignedUnit(),
                "Enviar protocolo de compania."
        ));

        signalService.create(validRequest(1L));

        verify(eventPublisher, times(1)).publishEvent(any(TropelSignalCreatedEvent.class));
    }

    private void mockExistingTropelAndGuardian() {
        when(tropelRepository.findById(1L)).thenReturn(Optional.of(tropel));
        when(guardianRepository.findById(1L)).thenReturn(Optional.of(guardian));
    }

    private SignalRequest validRequest(Long guardianId) {
        return new SignalRequest(
                1L,
                guardianId,
                "sensor-norte-7",
                "BipBop lleva varios ciclos emitiendo senales de alerta."
        );
    }
}
