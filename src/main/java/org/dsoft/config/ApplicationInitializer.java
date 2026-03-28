package org.dsoft.config;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class ApplicationInitializer {

    void onStart(@Observes StartupEvent ev) {
        System.out.println("NutriChef Backend initializing...");
        System.out.println("Application initialization complete");
    }
}