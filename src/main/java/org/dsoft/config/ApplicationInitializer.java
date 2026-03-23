package org.dsoft.config;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.dsoft.control.IngredientSubstitutionService;

@ApplicationScoped
public class ApplicationInitializer {

    @Inject
    IngredientSubstitutionService substitutionService;

    void onStart(@Observes StartupEvent ev) {
        System.out.println("NutriChef Backend initializing...");
        
        substitutionService.initializeDefaultSubstitutions();
        System.out.println("Default ingredient substitutions initialized");
        
        System.out.println("Application initialization complete");
    }
}