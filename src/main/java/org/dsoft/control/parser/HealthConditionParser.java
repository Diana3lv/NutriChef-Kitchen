package org.dsoft.control.parser;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dsoft.client.GroqClient;
import org.dsoft.control.result.HealthConditionValidationResult;
import org.dsoft.control.result.HealthStringValidationResult;

@ApplicationScoped
public class HealthConditionParser {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthConditionParser.class);
    
    @Inject
    GroqClient groqClient;
    
    private static final HealthConditionParserRuleBased ruleBased = new HealthConditionParserRuleBased();

    public Optional<String> cleanAndValidateHealthString(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        return validateWithLLM(input);
    }

    /**
     * Validate health condition with detailed result distinguishing between:
     * - SUCCESS: LLM validated and approved
     * - REJECTED: LLM explicitly rejected as invalid
     * - API_ERROR: LLM unavailable (network error, invalid key, etc.)
     */
    public HealthConditionValidationResult validateHealthConditionWithResult(String input) {
        if (input == null || input.isBlank()) {
            return HealthConditionValidationResult.rejected(input != null ? input : "");
        }
        
        if (groqClient == null) {
            logger.debug("GroqClient not available, treating as API error");
            return HealthConditionValidationResult.apiError(input);
        }
        
        try {
            String response = groqClient.generateResponse(buildValidationPrompt(input));
            
            if (response != null && response.trim().equalsIgnoreCase("yes")) {
                logger.debug("LLM validated '{}' as valid medical condition/intolerance", input);
                return HealthConditionValidationResult.success(input);
            } else if (response != null && response.trim().equalsIgnoreCase("no")) {
                logger.info("LLM rejected '{}' as invalid medical condition/intolerance", input);
                return HealthConditionValidationResult.rejected(input);
            } else {
                logger.warn("LLM returned unclear response for input '{}': {}", input, response);
                return HealthConditionValidationResult.apiError(input);
            }
        } catch (Exception e) {
            logger.warn("LLM validation failed for input '{}': {}", input, e.getMessage());
            return HealthConditionValidationResult.apiError(input);
        }
    }

    public HealthStringValidationResult validateAndCorrectHealthStringWithResult(String input) {
        if (input == null || input.isBlank()) {
            return HealthStringValidationResult.success(null);
        }

        String correctedTypos = autoCorrectHealthString(input);
        Optional<String> validated = cleanAndValidateHealthString(correctedTypos);
        return HealthStringValidationResult.success(validated.orElse(null));
    }

    private Optional<String> validateWithLLM(String input) {
        if (groqClient == null) {
            return Optional.empty();
        }
        try {
            String response = groqClient.generateResponse(buildValidationPrompt(input));
            if (response != null && response.trim().equalsIgnoreCase("yes")) {
                return Optional.of(input);
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.warn("LLM validation failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String buildValidationPrompt(String input) {
        return """
            You are a medical validation expert. Your task is to determine if the following input string represents a real medical condition, food intolerance, or allergy.

            - **REJECT** (respond with only "no") for:
                - Generic non-medical words (e.g., "none", "nothing", "house", "car", "test").
                - Gibberish or random characters (e.g., "asdfghjkl").
                - Single letters or numbers.
                - Negations or statements of absence (e.g., "I don't have any", "not applicable").

            - **ACCEPT** (respond with only "yes") for:
                - Legitimate medical conditions (e.g., "diabetes", "celiac disease", "hypertension", "wolfram syndrome").
                - Real food intolerances or allergies (e.g., "lactose intolerance", "peanut allergy", "gluten sensitivity").

            Input: "%s"

            Based on these rules, is the input a valid medical condition, intolerance, or allergy? Respond with ONLY "yes" or "no".
            """.formatted(input);
    }

    public String autoCorrectHealthString(String input) {
        if (input == null || input.isBlank() || groqClient == null) {
            return input;
        }
        try {
            String response = groqClient.generateResponse(buildAutoCorrectPrompt(input));
            if (response == null || response.isBlank()) {
                return input;
            }
            String extracted = extractFirstLineFromResponse(response);
            return isValidLLMResponse(extracted) ? extracted : input;
        } catch (Exception e) {
            logger.warn("Auto-correction failed: {}", e.getMessage());
            return input;
        }
    }

    private String extractFirstLineFromResponse(String response) {
        String trimmed = response.trim();
        return trimmed.contains("\n") ? trimmed.split("\n")[0].trim() : trimmed;
    }

    private boolean isValidLLMResponse(String response) {
        if (response == null || response.isBlank()) {
            return false;
        }
        int length = response.length();
        String lower = response.toLowerCase();
        boolean isRambling = lower.contains("could be") || lower.contains("possibility") || 
                           lower.contains("perhaps") || lower.contains("might be") || 
                           lower.matches(".*[?]{2,}.*");
        return length <= 100 && !isRambling;
    }

    private String buildAutoCorrectPrompt(String input) {
        return """
            You are a medical spell-checker. Correct any typos or spelling errors in the following \
            health condition or food intolerance string. Return ONLY the corrected string, nothing else.
            
            Examples:
            - "wolfram syndro.e" → "wolfram syndrome"
            - "lactse intolerance" → "lactose intolerance"
            - "celiak disease" → "celiac disease"
            - "wolfram syndrome" → "wolfram syndrome" (already correct)
            
            Input: "%s"
            
            Respond with ONLY the corrected string:
            """.formatted(input);
    }

    public Set<String> parseHealthConditions(String medicalConditions, String intolerances) {
        if (bothFieldsEmpty(medicalConditions, intolerances)) {
            return new HashSet<>();
        }
        try {
            return parseWithLLM(medicalConditions, intolerances);
        } catch (Exception e) {
            logger.warn("LLM parsing failed, falling back to rule-based: {}", e.getMessage());
            return ruleBased.parseHealthConditions(medicalConditions, intolerances);
        }
    }

    private boolean bothFieldsEmpty(String medicalConditions, String intolerances) {
        return (medicalConditions == null || medicalConditions.isBlank()) &&
               (intolerances == null || intolerances.isBlank());
    }
    
    private Set<String> parseWithLLM(String medicalConditions, String intolerances) {
        if (groqClient == null) {
            return ruleBased.parseHealthConditions(medicalConditions, intolerances);
        }
        String response = groqClient.generateResponse(buildPrompt(medicalConditions, intolerances));
        return response == null || response.isBlank()
            ? ruleBased.parseHealthConditions(medicalConditions, intolerances)
            : extractIngredientsFromResponse(response);
    }
    
    private String buildPrompt(String medicalConditions, String intolerances) {
        return """
            You are a nutritionist expert. Given the following health conditions and food intolerances, \
            provide ONLY a comma-separated list of specific ingredients to avoid. \
            No explanations, no extra text, just the ingredients list.
            
            Medical Conditions: %s
            Food Intolerances: %s
            
            Respond with ONLY a comma-separated list of ingredients to avoid. For example: \
            milk, cheese, butter, gluten, wheat, sugar
            """.formatted(
                medicalConditions != null && !medicalConditions.isBlank() ? medicalConditions : "none",
                intolerances != null && !intolerances.isBlank() ? intolerances : "none"
            );
    }
    
    private Set<String> extractIngredientsFromResponse(String response) {
        Set<String> ingredients = new HashSet<>();
        if (response == null || response.isBlank()) {
            return ingredients;
        }
        
        String[] items = response.split("[,;\\n]");
        for (String item : items) {
            String cleaned = item.trim().toLowerCase();
            cleaned = cleaned.replaceAll("^(the|a|and|or)\\s+", "")
                              .replaceAll("\\s*(is|are|or|and)\\s*", " ")
                              .trim();
            
            if (!cleaned.isEmpty() && 
                !cleaned.contains("example") && 
                !cleaned.contains("list") &&
                !cleaned.contains("avoid") &&
                cleaned.length() > 1) {
                ingredients.add(cleaned);
            }
        }
        return ingredients;
    }
}
