package com.tropelcare.signalengine.models;

import com.tropelcare.signalengine.models.enums.Climate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "sectors")
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String sectorCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Climate climate;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer currentLoad;

    @Column(nullable = false)
    private Integer stabilityLevel;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "sector")
    private List<Tropel> tropels = new ArrayList<>();
}
