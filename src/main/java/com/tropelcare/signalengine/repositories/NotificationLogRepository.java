package com.tropelcare.signalengine.repositories;

import com.tropelcare.signalengine.models.NotificationLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    List<NotificationLog> findBySignalIdOrderByCreatedAtDesc(Long signalId);
}
