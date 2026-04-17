package org.dsoft.control.parser;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dsoft.client.GroqClient;
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
            if (response == null || response.isBlank()) {
                return Optional.empty();
            }
            if (isSuspiciousResponse(response)) {
                logger.warn("LLM returned suspicious response for input '{}': {}", input, response);
                return Optional.empty();
            }
            return isMeaningfulResponse(response) ? Optional.of(input) : Optional.empty();
        } catch (Exception e) {
            logger.warn("LLM validation failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private boolean isMeaningfulResponse(String response) {
        String normalized = response.trim().toLowerCase();
        return normalized.startsWith("yes") || normalized.contains("meaningful") || normalized.contains("valid");
    }

    private boolean isSuspiciousResponse(String response) {
        if (response == null || response.isBlank()) {
            return false;
        }
        String lower = response.toLowerCase();
        return lower.contains("could be") || lower.contains("possibility") || 
               lower.contains("perhaps") || lower.contains("might be") ||
               lower.contains("hypothetical") || lower.contains("however") ||
               response.matches(".*[?]{2,}.*") ||
               response.split("\n").length > 2;  // Multi-paragraph response
    }

    private String buildValidationPrompt(String input) {
        return """
            You are a health assistant. Determine if the following represents a real medical condition \
            or food intolerance. Respond with ONLY "yes" or "no".
            
            Say "no" for: "none", "no intolerances", "i don't have", "nothing", "nope", "n/a", \
            or typos like "no intolrrances".
            
            Say "yes" for real conditions like: "diabetes", "lactose intolerance", "wolfram syndrome".
            
            Input: "%s"
            
            Respond with only "yes" or "no":
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
