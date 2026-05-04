package com.tropelcare.signalengine.repositories;

import com.tropelcare.signalengine.models.CareResponse;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareResponseRepository extends JpaRepository<CareResponse, Long> {

    Optional<CareResponse> findBySignalId(Long signalId);
}
