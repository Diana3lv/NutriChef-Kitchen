package org.dsoft.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dsoft.client.GroqClient;
import org.dsoft.entity.dto.IngredientDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

@ApplicationScoped
public class SubstitutionLLMService {

    private static final Logger logger = LoggerFactory.getLogger(SubstitutionLLMService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    GroqClient groqClient;

    public List<IngredientDTO> findSubstitutions(String ingredientName) {
        if (ingredientName == null || ingredientName.isBlank()) {
            return new ArrayList<>();
        }

        try {
            String response = getSubstitutionsFromLLM(ingredientName);
            logger.info("LLM response for substitutions of '{}': {}", ingredientName, response);
            return parseSubstitutionsAsJSON(response);
        } catch (Exception e) {
            logger.warn("LLM substitution lookup failed for ingredient '{}': {}", ingredientName, e.getMessage());
            return new ArrayList<>();
        }
    }

    private String getSubstitutionsFromLLM(String ingredientName) {
        String prompt = buildSubstitutionPrompt(ingredientName);
        String response = groqClient.generateResponse(prompt);
        
        if (response == null || response.isBlank()) {
            logger.warn("Empty response from LLM for ingredient: {}", ingredientName);
            return "";
        }
        
        return response;
    }

    private String buildSubstitutionPrompt(String ingredientName) {
        return """
            You are a culinary expert specializing in ingredient substitutions.
            For the ingredient "%s", provide substitutions as a JSON array of objects.
            
            Return ONLY a valid JSON array with this exact structure:
            [
              {
                "name": "substitution ingredient name",
                "unit": "unit of measurement",
                "ratio": "substitution ratio or proportion",
                "allergens": ["allergen1", "allergen2"]
              }
            ]
            
            Valid allergen enum values (use EXACTLY these):
            DAIRY, EGGS, SOY, WHEAT, TREE_NUTS, PEANUTS, FISH, SHELLFISH, GLUTEN, SESAME, MUSTARD, SULFITES, CELERY, LUPIN, MOLLUSKS
            
            Rules:
            - Each substitution must be a valid JSON object with "name", "unit", "ratio", and "allergens" fields
            - If the ingredient has no single substitution, return combinations as separate JSON objects
            - For "self-rising flour", return both "all-purpose flour" and "baking powder" as separate objects
            - "ratio" should express how much of the original ingredient the substitute replaces (e.g., "1:1", "3:4", "1 tbsp per 1 tsp", "3 tbsp per 1 egg")
            - If ratio is 1-to-1, use "1:1"
            - "allergens" must be an array of strings using ONLY the enum values listed above
            - Use standard measurement units (g, ml, tbsp, tsp, oz, etc.)
            - Do NOT include explanations or text outside the JSON array
            - Return empty array [] if no substitutions exist
            
            Examples:
            - For "butter": [{"name": "margarine", "unit": "g", "ratio": "1:1", "allergens": []}, {"name": "oil", "unit": "ml", "ratio": "3:4", "allergens": []}]
            - For "egg": [{"name": "flax egg", "unit": "tbsp", "ratio": "3 tbsp per 1 egg", "allergens": []}, {"name": "applesauce", "unit": "ml", "ratio": "1:1", "allergens": []}]
            - For "milk": [{"name": "almond milk", "unit": "ml", "ratio": "1:1", "allergens": ["TREE_NUTS"]}]
            - For "self-rising flour": [{"name": "all-purpose flour", "unit": "g", "ratio": "1:1", "allergens": ["WHEAT", "GLUTEN"]}, {"name": "baking powder", "unit": "tsp", "ratio": "1.5 tsp per 1 tsp flour", "allergens": []}]
            
            Ingredient: "%s"
            Response:""".formatted(ingredientName, ingredientName);
    }

    private List<IngredientDTO> parseSubstitutionsAsJSON(String response) {
        if (response == null || response.isBlank()) {
            logger.warn("Empty response from LLM");
            return new ArrayList<>();
        }

        try {
            // Extract JSON array from response (in case there's extra text)
            String jsonPart = response.trim();
            if (jsonPart.startsWith("[") && jsonPart.endsWith("]")) {
                IngredientDTO[] dtos = objectMapper.readValue(jsonPart, IngredientDTO[].class);
                return Arrays.asList(dtos);
            } else {
                logger.warn("Response does not contain valid JSON array: {}", response);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("Failed to parse substitutions JSON: {}", response, e);
            return new ArrayList<>();
        }
    }
}
