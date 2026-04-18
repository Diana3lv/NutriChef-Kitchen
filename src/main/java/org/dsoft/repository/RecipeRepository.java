package org.dsoft.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.dsoft.entity.model.Allergen;
import org.dsoft.entity.model.Ingredient;
import org.dsoft.entity.model.Recipe;

@ApplicationScoped
public class RecipeRepository implements PanacheRepository<Recipe> {

    public List<Recipe> findCompatibleRecipes(Set<String> avoidedIngredients) {
        if (avoidedIngredients == null || avoidedIngredients.isEmpty()) {
            return listAll();
        }
        
        return listAll().stream()
            .filter(recipe -> isRecipeCompatible(recipe, avoidedIngredients))
            .collect(Collectors.toList());
    }

    private boolean isRecipeCompatible(Recipe recipe, Set<String> avoidedIngredients) {
        if (recipe.recipeIngredients == null || recipe.recipeIngredients.isEmpty()) {
            return true;
        }
        
        return recipe.recipeIngredients.stream()
            .noneMatch(recipeIngredient -> 
                isIngredientAvoided(recipeIngredient.ingredient, avoidedIngredients)
            );
    }

    private boolean isIngredientAvoided(Ingredient ingredient, Set<String> avoidedIngredients) {
        if (ingredient == null) {
            return false;
        }
        
        boolean isForbidden = isIngrediendForbidden(ingredient, avoidedIngredients);
        
        if (!isForbidden) {
            return false;
        }
        
        // If substitutions of forbidden ingredients exist, the recipe can still be used
        return ingredient.substitutions == null || ingredient.substitutions.isEmpty();
    }

    private boolean isIngrediendForbidden(Ingredient ingredient, Set<String> avoidedIngredients) {
        String ingredientNameLower = ingredient.name.toLowerCase();
        
        for (String avoided : avoidedIngredients) {
            if (ingredientNameLower.contains(avoided.toLowerCase())) {
                return true;
            }
        }
        
        // Check if any of the ingredient's allergens match avoided ingredients
        if (ingredient.allergens != null && !ingredient.allergens.isEmpty()) {
            for (Allergen allergen : ingredient.allergens) {
                if (avoidedIngredients.contains(allergen.name().toLowerCase())) {
                    return true;
                }
                // Also check common allergen names (e.g., "EGG" -> "eggs", "DAIRY" -> "cheese", "milk", etc.)
                if (allergenNameMatches(allergen.toString(), avoidedIngredients)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Check if an allergen (in string form) matches any avoided ingredients.
     * Maps allergen enum names to their ingredient equivalents.
     */
    private boolean allergenNameMatches(String allergenName, Set<String> avoidedIngredients) {
        String allergenLower = allergenName.toLowerCase();
        return avoidedIngredients.stream()
            .anyMatch(avoided -> avoided.contains(allergenLower) || allergenLower.contains(avoided));
    }
}
