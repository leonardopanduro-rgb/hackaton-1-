package com.tropelcare.signalengine.repositories;

import com.tropelcare.signalengine.models.TropelSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TropelSignalRepository extends JpaRepository<TropelSignal, Long>, JpaSpecificationExecutor<TropelSignal> {
}
