package org.dsoft.service;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class NutritionService {

    private static final Logger logger = LoggerFactory.getLogger(NutritionService.class);

    @ConfigProperty(name = "spoonacular.api.key", defaultValue = "")
    String spoonacularApiKey;

    /**
     * Fetch nutrition info for a recipe from Spoonacular API
     * This requires a valid Spoonacular API key in application.properties
     * Returns null if API key not configured or request fails
     */
    public RecipeNutritionDTO getRecipeNutrition(Long spoonacularRecipeId) {
        if (spoonacularApiKey == null || spoonacularApiKey.isEmpty()) {
            logger.warn("Spoonacular API key not configured");
            return null;
        }

        try {
            SpoonacularClient client = QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create("https://api.spoonacular.com"))
                .build(SpoonacularClient.class);

            SpoonacularNutritionDTO response = client.getRecipeNutrition(
                spoonacularRecipeId,
                spoonacularApiKey
            );

            if (response != null) {
                return new RecipeNutritionDTO(
                    response.calories,
                    response.protein,
                    response.fat,
                    response.carbs,
                    response.fiber
                );
            }
        } catch (Exception e) {
            logger.error("Error fetching nutrition from Spoonacular: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Calculate nutrition per serving if total nutrition is known
     */
    public RecipeNutritionDTO calculatePerServing(RecipeNutritionDTO totalNutrition, Integer servings) {
        if (totalNutrition == null || servings == null || servings <= 0) {
            return totalNutrition;
        }

        return new RecipeNutritionDTO(
            totalNutrition.calories != null ? (int) Math.round((double) totalNutrition.calories / servings) : null,
            totalNutrition.protein != null ? totalNutrition.protein / servings : null,
            totalNutrition.fat != null ? totalNutrition.fat / servings : null,
            totalNutrition.carbs != null ? totalNutrition.carbs / servings : null,
            totalNutrition.fiber != null ? totalNutrition.fiber / servings : null
        );
    }

    /**
     * REST client interface for Spoonacular API
     */
    public interface SpoonacularClient {
        @GET
        @Path("/recipes/{id}/information")
        SpoonacularNutritionDTO getRecipeNutrition(
            @PathParam("id") Long recipeId,
            @QueryParam("apiKey") String apiKey
        );
    }

    /**
     * Spoonacular API response DTO
     */
    public static class SpoonacularNutritionDTO {
        public class Nutrition {
            public String name;
            public Double amount;
            public String unit;
        }

        public String title;
        public Nutrition nutrients;

        // Extracted nutrition info
        public Integer calories;
        public Double protein;
        public Double fat;
        public Double carbs;
        public Double fiber;
    }

    /**
     * Recipe nutrition info to store
     */
    public static class RecipeNutritionDTO {
        public Integer calories;
        public Double protein;
        public Double fat;
        public Double carbs;
        public Double fiber;

        public RecipeNutritionDTO() {}

        public RecipeNutritionDTO(
            Integer calories,
            Double protein,
            Double fat,
            Double carbs,
            Double fiber
        ) {
            this.calories = calories;
            this.protein = protein;
            this.fat = fat;
            this.carbs = carbs;
            this.fiber = fiber;
        }
    }
}