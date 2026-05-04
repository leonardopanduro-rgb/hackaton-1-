package com.tropelcare.signalengine.config;

import com.tropelcare.signalengine.models.Guardian;
import com.tropelcare.signalengine.repositories.GuardianRepository;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final GuardianRepository guardianRepository;
    private final String adminName;
    private final String adminEmail;
    private final String adminNotificationEmail;

    public DataInitializer(GuardianRepository guardianRepository,
                           @Value("${app.admin.display-name}") String adminName,
                           @Value("${app.admin.email}") String adminEmail,
                           @Value("${app.admin.notification-email}") String adminNotificationEmail) {
        this.guardianRepository = guardianRepository;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminNotificationEmail = adminNotificationEmail;
    }

    @Override
    public void run(String... args) {
        if (guardianRepository.existsByEmail(adminEmail)) {
            return;
        }

        Guardian guardian = new Guardian();
        guardian.setDisplayName(adminName);
        guardian.setEmail(adminEmail);
        guardian.setNotificationEmail(adminNotificationEmail);
        guardian.setCreatedAt(Instant.now());

        guardianRepository.save(guardian);
    }
}
