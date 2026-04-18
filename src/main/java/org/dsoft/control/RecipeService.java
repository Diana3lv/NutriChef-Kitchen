package org.dsoft.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.dsoft.entity.dto.NutritionProfileDTO;
import org.dsoft.entity.model.Recipe;
import org.dsoft.repository.RecipeRepository;

@ApplicationScoped
public class RecipeService {

    @Inject
    RecipeRepository recipeRepository;

    @Inject
    NutritionProfileService nutritionProfileService;

    public List<Recipe> getAll() {
        return recipeRepository.listAll();
    }

    public Optional<Recipe> getById(Long id) {
        return recipeRepository.findByIdOptional(id);
    }

    @Transactional
    public void create(Recipe recipe) {
        recipeRepository.persist(recipe);
    }

    @Transactional
    public Optional<Recipe> update(Long id, Recipe updatedRecipe) {
        Optional<Recipe> recipe = recipeRepository.findByIdOptional(id);
        if (recipe.isPresent()) {
            Recipe entity = recipe.get();
            entity.title = updatedRecipe.title;
            entity.description = updatedRecipe.description;
            entity.instructions = updatedRecipe.instructions;
            entity.prepTimeMinutes = updatedRecipe.prepTimeMinutes;
            entity.cookTimeMinutes = updatedRecipe.cookTimeMinutes;
            entity.servings = updatedRecipe.servings;
            entity.difficulty = updatedRecipe.difficulty;
            entity.imageUrl = updatedRecipe.imageUrl;
            entity.sourceUrl = updatedRecipe.sourceUrl;
            entity.sourceApi = updatedRecipe.sourceApi;
            // ingredients are handled separately or via cascade depending on mapping
        }
        return recipe;
    }

    @Transactional
    public boolean delete(Long id) {
        return recipeRepository.deleteById(id);
    }

    /**
     * Get all recipes compatible with the user's nutrition profile.
     * Orchestration layer: coordinates between repositories and business logic.
     * 
     * Returns all recipes if user has no profile.
     */
    public List<Recipe> getRecipesForUserNutritionProfile(Long userId) {
        try {
            NutritionProfileDTO profileDTO = nutritionProfileService.getNutritionProfileByUserId(userId);
            
            // If no avoided ingredients, return all recipes
            if (profileDTO.getParsedAvoidIngredients() == null || profileDTO.getParsedAvoidIngredients().isEmpty()) {
                return getAll();
            }
            
            // Use repository to get filtered recipes based on avoided ingredients
            return recipeRepository.findCompatibleRecipes(profileDTO.getParsedAvoidIngredients());
        } catch (Exception e) {
            // If profile not found or error occurs, return all recipes
            return getAll();
        }
    }
}