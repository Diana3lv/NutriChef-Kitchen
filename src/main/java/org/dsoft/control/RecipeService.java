package org.dsoft.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.dsoft.entity.dto.CreateRecipeRequestDTO;
import org.dsoft.entity.dto.NutritionProfileDTO;
import org.dsoft.entity.model.Ingredient;
import org.dsoft.entity.model.Recipe;
import org.dsoft.entity.model.RecipeIngredient;
import org.dsoft.entity.model.RecipeTag;
import org.dsoft.repository.IngredientRepository;
import org.dsoft.repository.RecipeRepository;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class RecipeService {

    @Inject
    RecipeRepository recipeRepository;

    @Inject
    IngredientRepository ingredientRepository;

    @Inject
    NutritionProfileService nutritionProfileService;

    public List<Recipe> getAll() {
        return recipeRepository.listAll();
    }

    public List<Recipe> search(String query) {
        return recipeRepository.searchByTitle(query);
    }

    public List<Recipe> getByTag(RecipeTag tag) {
        return recipeRepository.findByTag(tag);
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

    @Transactional
    public Recipe createFromRequest(CreateRecipeRequestDTO request) {
        Recipe recipe = new Recipe();
        applyRequestToRecipe(recipe, request);
        recipe.sourceApi = "ADMIN";
        recipeRepository.persist(recipe);
        addIngredients(recipe, request.ingredients);
        return recipe;
    }

    @Transactional
    public Optional<Recipe> updateFromRequest(Long id, CreateRecipeRequestDTO request) {
        Optional<Recipe> recipe = recipeRepository.findByIdOptional(id);
        if (recipe.isPresent()) {
            Recipe entity = recipe.get();
            applyRequestToRecipe(entity, request);
            if (request.imageUrl != null) {
                entity.imageUrl = request.imageUrl;
            }
            entity.recipeIngredients.clear();
            addIngredients(entity, request.ingredients);
        }
        return recipe;
    }

    private void applyRequestToRecipe(Recipe recipe, CreateRecipeRequestDTO request) {
        recipe.title = request.title;
        recipe.description = request.description;
        recipe.instructions = request.instructions;
        recipe.prepTimeMinutes = request.prepTimeMinutes;
        recipe.cookTimeMinutes = request.cookTimeMinutes;
        recipe.servings = request.servings;
        if (request.difficulty != null) {
            try {
                recipe.difficulty = Recipe.Difficulty.valueOf(request.difficulty);
            } catch (IllegalArgumentException ignored) { }
        }
        recipe.imageUrl = request.imageUrl;
        if (request.tags != null) {
            recipe.tags = request.tags.stream()
                .filter(t -> t != null && !t.isBlank())
                .map(t -> { try { return RecipeTag.valueOf(t); } catch (IllegalArgumentException e) { return null; } })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        } else {
            if (recipe.tags == null) recipe.tags = new HashSet<>();
        }
    }

    private void addIngredients(Recipe recipe, List<CreateRecipeRequestDTO.IngredientItemDTO> items) {
        if (items == null) return;
        for (CreateRecipeRequestDTO.IngredientItemDTO item : items) {
            ingredientRepository.findByIdOptional(item.ingredientId).ifPresent(ingredient -> {
                RecipeIngredient ri = new RecipeIngredient();
                ri.recipe = recipe;
                ri.ingredient = ingredient;
                ri.quantity = item.quantity;
                ri.persist();
                recipe.recipeIngredients.add(ri);
            });
        }
    }

    public String saveImage(FileUpload file) {
        String originalName = file.fileName() != null ? file.fileName() : "image";
        String safeExtension = "";
        int dotIdx = originalName.lastIndexOf('.');
        if (dotIdx >= 0) {
            String ext = originalName.substring(dotIdx).toLowerCase().replaceAll("[^.a-z0-9]", "");
            if (List.of(".jpg", ".jpeg", ".png", ".gif", ".webp").contains(ext)) {
                safeExtension = ext;
            }
        }
        String filename = UUID.randomUUID().toString() + safeExtension;
        Path uploadDir = Paths.get("uploads", "recipes");
        try {
            Files.createDirectories(uploadDir);
            Files.copy(file.uploadedFile(), uploadDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return "/recipes/images/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image", e);
        }
    }

    public Optional<Path> getImagePath(String filename) {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return Optional.empty();
        }
        Path imagePath = Paths.get("uploads", "recipes", filename);
        return Files.exists(imagePath) ? Optional.of(imagePath) : Optional.empty();
    }

    public List<Recipe> getRecipesForUserNutritionProfile(Long userId) {
        try {
            NutritionProfileDTO profileDTO = nutritionProfileService.getNutritionProfileByUserId(userId);
            
            if (profileDTO.getParsedAvoidIngredients() == null || profileDTO.getParsedAvoidIngredients().isEmpty()) {
                return getAll();
            }
            
            return recipeRepository.findCompatibleRecipes(profileDTO.getParsedAvoidIngredients());
        } catch (Exception e) {
            // If profile not found or error occurs, return all recipes
            return getAll();
        }
    }
}