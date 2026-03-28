package org.dsoft.control;

import java.util.HashSet;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dsoft.client.GroqClient;

@ApplicationScoped
public class HealthConditionParser {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthConditionParser.class);
    
    @Inject
    GroqClient groqClient;
    
    private static final HealthConditionParserRuleBased ruleBased = new HealthConditionParserRuleBased();

    public Set<String> parseHealthConditions(String medicalConditions, String intolerances) {
        if ((medicalConditions == null || medicalConditions.isBlank()) && 
            (intolerances == null || intolerances.isBlank())) {
            return new HashSet<>();
        }
        
        try {
            // Try LLM-based parsing
            return parseWithLLM(medicalConditions, intolerances);
        } catch (Exception e) {
            logger.warn("LLM parsing failed, falling back to rule-based parser: {}", e.getMessage());
            // Fallback to rule-based parsing
            return ruleBased.parseHealthConditions(medicalConditions, intolerances);
        }
    }
    
    private Set<String> parseWithLLM(String medicalConditions, String intolerances) {
        if (groqClient == null) {
            logger.warn("OllamaClient not available, using rule-based parser");
            return ruleBased.parseHealthConditions(medicalConditions, intolerances);
        }
        
        String prompt = buildPrompt(medicalConditions, intolerances);
        String response = groqClient.generateResponse(prompt);
        
        if (response == null || response.isBlank()) {
            logger.warn("Empty response from LLM, falling back to rule-based");
            return ruleBased.parseHealthConditions(medicalConditions, intolerances);
        }
        
        return extractIngredientsFromResponse(response);
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