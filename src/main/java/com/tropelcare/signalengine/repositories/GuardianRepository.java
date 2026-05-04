package com.tropelcare.signalengine.repositories;

import com.tropelcare.signalengine.models.Guardian;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuardianRepository extends JpaRepository<Guardian, Long> {

    boolean existsByEmail(String email);

    Optional<Guardian> findByEmail(String email);
}
