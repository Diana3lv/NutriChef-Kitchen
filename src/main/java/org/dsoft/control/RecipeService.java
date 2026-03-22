package org.dsoft.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import org.dsoft.entity.model.Recipe;

@ApplicationScoped
public class RecipeService {

    public List<Recipe> getAll() {
        return Recipe.listAll();
    }

    public Recipe getById(Long id) {
        return Recipe.findById(id);
    }

    @Transactional
    public void create(Recipe recipe) {
        recipe.persist();
    }

    @Transactional
    public Recipe update(Long id, Recipe recipe) {
        Recipe entity = Recipe.findById(id);
        if (entity == null) {
            return null;
        }
        entity.title = recipe.title;
        entity.description = recipe.description;
        entity.instructions = recipe.instructions;
        entity.prepTimeMinutes = recipe.prepTimeMinutes;
        entity.cookTimeMinutes = recipe.cookTimeMinutes;
        entity.servings = recipe.servings;
        entity.difficulty = recipe.difficulty;
        entity.imageUrl = recipe.imageUrl;
        entity.sourceUrl = recipe.sourceUrl;
        entity.sourceApi = recipe.sourceApi;
        // ingredients are handled separately or via cascade depending on mapping
        
        return entity;
    }

    @Transactional
    public boolean delete(Long id) {
        return Recipe.deleteById(id);
    }
}