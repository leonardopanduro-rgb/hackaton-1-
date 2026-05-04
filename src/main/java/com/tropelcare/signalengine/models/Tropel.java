package com.tropelcare.signalengine.models;

import com.tropelcare.signalengine.models.enums.Species;
import com.tropelcare.signalengine.models.enums.VitalState;
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
@Table(name = "tropels")
public class Tropel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Species species;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private VitalState vitalState;

    @Column(nullable = false)
    private Integer energyLevel;

    @Column(nullable = false)
    private Integer chaosIndex;

    @Column(nullable = false)
    private Integer mutationStage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Guardian guardian;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "tropel")
    private List<TropelSignal> signals = new ArrayList<>();
}
