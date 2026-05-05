package com.tropelcare.signalengine.events;

import com.tropelcare.signalengine.models.NotificationLog;
import com.tropelcare.signalengine.models.Tropel;
import com.tropelcare.signalengine.models.TropelSignal;
import com.tropelcare.signalengine.models.enums.NotificationStatus;
import com.tropelcare.signalengine.models.enums.SignalStatus;
import com.tropelcare.signalengine.repositories.NotificationLogRepository;
import com.tropelcare.signalengine.repositories.TropelSignalRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TropelSignalNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(TropelSignalNotificationListener.class);

    private final TropelSignalRepository signalRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final JavaMailSender mailSender;

    @Async("tropelTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(TropelSignalCreatedEvent event) {
        TropelSignal signal = signalRepository.findById(event.signalId())
                .orElseThrow(() -> new IllegalStateException("No existe la senal " + event.signalId()));

        signal.setStatus(SignalStatus.PROCESANDO);
        signal.setUpdatedAt(Instant.now());

        String subject = buildSubject(signal);
        NotificationLog notificationLog = new NotificationLog();
        notificationLog.setSignal(signal);
        notificationLog.setRecipientEmail(signal.getGuardian().getNotificationEmail());
        notificationLog.setSubject(subject);
        notificationLog.setCreatedAt(Instant.now());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(signal.getGuardian().getNotificationEmail());
            message.setSubject(subject);
            message.setText(buildBody(signal));
            mailSender.send(message);

            signal.setStatus(SignalStatus.ATENDIDA);
            signal.setUpdatedAt(Instant.now());
            notificationLog.setNotifStatus(NotificationStatus.SENT);
            notificationLog.setSentAt(Instant.now());
        } catch (Exception exception) {
            log.error("Error enviando notificacion de TropelCare", exception);
            signal.setStatus(SignalStatus.ERROR);
            signal.setUpdatedAt(Instant.now());
            notificationLog.setNotifStatus(NotificationStatus.FAILED);
            notificationLog.setErrorMessage(exception.getMessage());
        }

        signalRepository.save(signal);
        notificationLogRepository.save(notificationLog);

        System.out.println("[TROPEL-LOG] Signal ID: " + signal.getId()
                + " | Tropel: " + signal.getTropel().getName()
                + " | Type: " + signal.getSignalType().name()
                + " | Severity: " + signal.getSeverity().name()
                + " | Unit: " + signal.getAssignedUnit()
                + " | Thread: " + Thread.currentThread().getName()
                + " | Status: " + signal.getStatus().name());
    }

    private String buildSubject(TropelSignal signal) {
        return "[TROPELCARE] " + signal.getSignalType().name()
                + " detectada en " + signal.getTropel().getName()
                + " | Severidad " + signal.getSeverity().name();
    }

    private String buildBody(TropelSignal signal) {
        Tropel tropel = signal.getTropel();
        return """
                Hola %s,

                Tu Tropel ha emitido una señal que requiere atención.

                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                Señal ID         : #%d
                Tropel           : %s (%s)
                Tipo de señal    : %s
                Severidad        : %s
                Unidad asignada  : %s
                Acción sugerida  : %s
                Estado vital     : %s
                Nivel de energía : %d/100
                Índice de caos   : %d/100
                Etapa mutación   : %d/5
                Registrada       : %s
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                Señal original:
                "%s"

                — TropelCare Signal Engine, Tuckersoft
                """.formatted(
                signal.getGuardian().getDisplayName(),
                signal.getId(),
                tropel.getName(),
                tropel.getSpecies().name(),
                signal.getSignalType().name(),
                signal.getSeverity().name(),
                signal.getAssignedUnit(),
                signal.getRecommendedAction(),
                tropel.getVitalState().name(),
                tropel.getEnergyLevel(),
                tropel.getChaosIndex(),
                tropel.getMutationStage(),
                signal.getCreatedAt(),
                signal.getRawContent()
        );
    }
}
