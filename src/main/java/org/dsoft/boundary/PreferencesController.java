package org.dsoft.boundary;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.dsoft.entity.model.Allergen;
import org.dsoft.entity.model.DietaryPreference;
import org.jetbrains.annotations.NotNull;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


@Path("/api/nutrition/preferences")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class PreferencesController {

    @GET
    @Path("/allergens")
    @SuppressWarnings("null") //
    public List<@NotNull OptionItem> getAvailableAllergens() {
        return Arrays.asList(Allergen.values())
                .stream()
                .map(allergen -> new OptionItem(
                    allergen.name(),
                    formatLabel(allergen.name())
                ))
                .toList();
    }

    @GET
    @Path("/dietary-preferences")
    @SuppressWarnings("null") //
    public List<@NotNull OptionItem> getAvailableDietaryPreferences() {
        return Arrays.asList(DietaryPreference.values())
                .stream()
                .map(pref -> new OptionItem(
                    pref.name(),
                    formatLabel(pref.name())
                ))
                .toList();
    }

    private String formatLabel(String enumValue) {
        return Arrays.stream(enumValue.split("_"))
                .map(word -> word.charAt(0) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    public static class OptionItem {
        public String apiValue;
        public String label;

        public OptionItem(String apiValue, String label) {
            this.apiValue = apiValue;
            this.label = label;
        }
    }
}