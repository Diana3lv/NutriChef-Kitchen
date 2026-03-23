package org.dsoft.control;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.dsoft.entity.model.Recipe;
import org.dsoft.entity.model.DietaryPreference;
import org.dsoft.entity.model.Allergen;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Applies hard filters for recipe compatibility with user's nutrition profile
 * 
 * Hard Filters are non-negotiable rules that must always exclude recipes:
 * - Allergens: User cannot consume these ingredients
 * - Dietary Preferences: Recipe must match user's dietary preferences
 * - Avoided Ingredients: Parsed from health conditions & intolerances
 */

@ApplicationScoped
public class RecipeFilterService {

    // Remove recipes containing user's allergens
    public List<Recipe> filterByAllergens(List<Recipe> recipes, List<Allergen> userAllergens) {
        if (userAllergens == null || userAllergens.isEmpty()) {
            return recipes;
        }

        return recipes.stream()
                .filter(recipe -> {
                    if (recipe.getAllergens() == null) {
                        return true;
                    }

                    return recipe.getAllergens().stream()
                            .noneMatch(userAllergens::contains);
                })
                .collect(Collectors.toList());
    }

    // Remove recipes that do not match user's dietary preferences
    public List<Recipe> filterByDietaryPreferences(List<Recipe> recipes, List<DietaryPreference> userPreferences) {
        if (userPreferences == null || userPreferences.isEmpty()) {
            return recipes;
        }

        return recipes.stream()
                .filter(recipe -> {
                    if (recipe.getDietaryPreferences() == null || recipe.getDietaryPreferences().isEmpty()) {
                        return true;
                    }

                    return userPreferences.stream()
                            .allMatch(pref -> recipe.getDietaryPreferences().contains(pref));
                })
                .collect(Collectors.toList());
    }

    // Remove recipes with ingredients user must avoid (based on parsed medical conditions & food intolerances)
    public List<Recipe> filterByAvoidIngredients(List<Recipe> recipes, Set<String> avoidIngredients) {
        if (avoidIngredients == null || avoidIngredients.isEmpty()) {
            return recipes;
        }

        return recipes.stream()
                .filter(recipe -> {
                    if (recipe.getIngredients() == null) {
                        return true;
                    }

                    return recipe.getIngredients().stream()
                            .noneMatch(ing -> avoidIngredients.stream()
                                    .anyMatch(avoid -> ing.name.toLowerCase()
                                            .contains(avoid.toLowerCase())));
                })
                .collect(Collectors.toList());
    }

    public List<Recipe> applyAllHardFilters(
            List<Recipe> recipes,
            List<Allergen> userAllergens,
            List<DietaryPreference> userPreferences,
            Set<String> avoidIngredients) {

        recipes = filterByAllergens(recipes, userAllergens);
        recipes = filterByDietaryPreferences(recipes, userPreferences);
        recipes = filterByAvoidIngredients(recipes, avoidIngredients);

        return recipes;
    }
}