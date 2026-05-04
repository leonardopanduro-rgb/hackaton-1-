package com.tropelcare.signalengine.models;

import com.tropelcare.signalengine.models.enums.Severity;
import com.tropelcare.signalengine.models.enums.SignalStatus;
import com.tropelcare.signalengine.models.enums.SignalType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tropel_signals")
public class TropelSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Tropel tropel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Guardian guardian;

    @Column(nullable = false, length = 80)
    private String senderTag;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SignalType signalType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Severity severity;

    @Column(nullable = false, length = 80)
    private String assignedUnit;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String recommendedAction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SignalStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToOne(mappedBy = "signal")
    private CareResponse careResponse;

    @OneToMany(mappedBy = "signal")
    private List<NotificationLog> notificationLogs = new ArrayList<>();
}
