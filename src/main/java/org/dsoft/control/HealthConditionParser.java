package org.dsoft.control;

import java.util.HashSet;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HealthConditionParser {

    public Set<String> parseHealthConditions(String healthConditions, String intolerances) {
        Set<String> avoidIngredients = new HashSet<>();
        
        String combinedInput = (healthConditions != null ? healthConditions : "") + " " +
                              (intolerances != null ? intolerances : "");
        String lowerInput = combinedInput.toLowerCase();

        // Dairy-related
        if (lowerInput.contains("lactose") || lowerInput.contains("dairy allergy")) {
            avoidIngredients.addAll(Set.of("milk", "cheese", "butter", "cream", "yogurt", "whey"));
        }

        // Gluten-related
        if (lowerInput.contains("celiac") || lowerInput.contains("gluten")) {
            avoidIngredients.addAll(Set.of("gluten", "wheat", "barley", "rye", "flour", "bread"));
        }

        // Nut allergies
        if (lowerInput.contains("peanut")) {
            avoidIngredients.addAll(Set.of("peanuts", "peanut oil", "peanut butter"));
        }
        if (lowerInput.contains("tree nut") || lowerInput.contains("almond") || 
            lowerInput.contains("walnut") || lowerInput.contains("cashew")) {
            avoidIngredients.addAll(Set.of("nuts", "almond", "walnut", "cashew", "hazelnut", "pecan"));
        }

        // Shellfish allergies
        if (lowerInput.contains("shellfish") || lowerInput.contains("seafood")) {
            avoidIngredients.addAll(Set.of("shrimp", "crab", "lobster", "oyster", "mussels", "scallops"));
        }

        // Egg allergy
        if (lowerInput.contains("egg allergy") || lowerInput.contains("egg intolerance")) {
            avoidIngredients.addAll(Set.of("eggs", "egg", "mayonnaise"));
        }

        // Soy allergy
        if (lowerInput.contains("soy")) {
            avoidIngredients.addAll(Set.of("soy", "tofu", "soy sauce", "soy milk"));
        }

        // Diabetes
        if (lowerInput.contains("diabetic") || lowerInput.contains("diabetes")) {
            avoidIngredients.addAll(Set.of("sugar", "honey", "maple syrup", "corn syrup"));
        }

        // High blood pressure
        if (lowerInput.contains("hypertension") || lowerInput.contains("high blood pressure")) {
            avoidIngredients.addAll(Set.of("salt", "sodium"));
        }

        return avoidIngredients;
    }
}