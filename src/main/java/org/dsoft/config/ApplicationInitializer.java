package org.dsoft.config;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.dsoft.control.StartupTimestampService;

@ApplicationScoped
public class ApplicationInitializer {

    @Inject
    StartupTimestampService startupTimestampService;

    void onStart(@Observes StartupEvent ev) {
        System.out.println("NutriChef Backend initializing...");
        
        // Set the startup timestamp - all tokens issued before this will be invalidated
        long currentTimestamp = System.currentTimeMillis() / 1000; // Convert to seconds
        startupTimestampService.setStartupTimestamp(currentTimestamp);
        
        System.out.println("Application initialization complete");
        System.out.println("All user sessions invalidated for fresh start");
    }
}