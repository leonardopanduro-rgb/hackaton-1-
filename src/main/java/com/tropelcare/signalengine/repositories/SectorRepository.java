package com.tropelcare.signalengine.repositories;

import com.tropelcare.signalengine.models.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectorRepository extends JpaRepository<Sector, Long> {

    boolean existsBySectorCode(String sectorCode);
}
