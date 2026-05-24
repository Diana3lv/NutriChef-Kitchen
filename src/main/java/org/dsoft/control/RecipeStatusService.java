package org.dsoft.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dsoft.entity.dto.RecipeFeedbackDTO;
import org.dsoft.entity.dto.SubmitFeedbackRequest;
import org.dsoft.entity.dto.UserRecipeStatusDTO;
import org.dsoft.entity.model.Recipe;
import org.dsoft.entity.model.RecipeCollectionType;
import org.dsoft.entity.model.RecipeFeedback;
import org.dsoft.entity.model.RecipeStatus;
import org.dsoft.entity.model.User;
import org.dsoft.entity.model.UserRecipeStatus;
import org.dsoft.repository.RecipeFeedbackRepository;
import org.dsoft.repository.RecipeRepository;
import org.dsoft.repository.UserRecipeStatusRepository;
import org.dsoft.service.ShoppingListService;

@ApplicationScoped
public class RecipeStatusService {

    @Inject
    UserRecipeStatusRepository statusRepository;

    @Inject
    RecipeFeedbackRepository feedbackRepository;

    @Inject
    ShoppingListService shoppingListService;

    @Inject
    InventoryService inventoryService;

    @Inject
    UserCollectionService collectionService;

    @Inject
    RecipeRepository recipeRepository;

    /**
     * Mark a recipe as IN_PROGRESS (user wants to cook it).
     * Populates the shopping list with all ingredients needed.
     * Idempotent: if already IN_PROGRESS, shopping list is re-synced.
     * If previously DONE: resets to IN_PROGRESS so the user can make it again.
     */
    @Transactional
    public UserRecipeStatusDTO markInProgress(Long userId, Long recipeId) {
        User user = User.findById(userId);
        Recipe recipe = recipeRepository.findById(recipeId);
        if (user == null || recipe == null) {
            throw new IllegalArgumentException("User or recipe not found");
        }

        UserRecipeStatus status = statusRepository.findByUserAndRecipe(userId, recipeId)
            .orElseGet(() -> {
                UserRecipeStatus s = new UserRecipeStatus();
                s.user = user;
                s.recipe = recipe;
                s.startedAt = LocalDateTime.now();
                return s;
            });

        status.status = RecipeStatus.IN_PROGRESS;
        if (status.startedAt == null) {
            status.startedAt = LocalDateTime.now();
        }
        status.completedAt = null;
        statusRepository.persist(status);

        // Populate or update the shopping list for this recipe
        shoppingListService.addRecipeIngredientsToList(userId, recipeId);

        return toDTO(status, userId);
    }

    /**
     * Mark a recipe as DONE.
     * - VALIDATES that user has all necessary ingredients before allowing completion.
     * - Upserts feedback (rating + notes) if provided.
     * - Subtracts ingredients from inventory.
     * - Reduces shopping list base quantities.
     * - Syncs shopping list with current inventory.
     * - Adds to the legacy DONE collection for backward compat.
     */
    @Transactional
    public UserRecipeStatusDTO markDone(Long userId, Long recipeId, SubmitFeedbackRequest feedback) {
        User user = User.findById(userId);
        Recipe recipe = recipeRepository.findById(recipeId);
        if (user == null || recipe == null) {
            throw new IllegalArgumentException("User or recipe not found");
        }

        // ✓ VALIDATE: User has all necessary ingredients
        List<InventoryService.MissingIngredientInfo> missingIngredients =
            inventoryService.validateRecipeIngredientsAvailable(userId, recipeId);
        if (!missingIngredients.isEmpty()) {
            throw new IllegalStateException("Insufficient inventory");
        }

        UserRecipeStatus status = statusRepository.findByUserAndRecipe(userId, recipeId)
            .orElseGet(() -> {
                UserRecipeStatus s = new UserRecipeStatus();
                s.user = user;
                s.recipe = recipe;
                s.startedAt = LocalDateTime.now();
                return s;
            });

        status.status = RecipeStatus.DONE;
        status.completedAt = LocalDateTime.now();
        status.cookCount = status.cookCount + 1;
        statusRepository.persist(status);

        // Upsert feedback if a valid rating was submitted
        if (feedback != null && feedback.rating >= 1 && feedback.rating <= 5) {
            RecipeFeedback fb = feedbackRepository.findByUserAndRecipe(userId, recipeId)
                .orElseGet(() -> {
                    RecipeFeedback f = new RecipeFeedback();
                    f.user = user;
                    f.recipe = recipe;
                    f.createdAt = LocalDateTime.now();
                    return f;
                });
            fb.rating = feedback.rating;
            fb.likedNotes = feedback.likedNotes;
            fb.improvementNotes = feedback.improvementNotes;
            fb.updatedAt = LocalDateTime.now();
            feedbackRepository.persist(fb);
        }

        // Inventory + shopping list adjustments
        shoppingListService.reduceBaseQuantityForCookedRecipe(userId, recipeId);
        inventoryService.subtractRecipeIngredients(userId, recipeId);
        shoppingListService.syncWithInventory(userId);

        // Also add to legacy DONE collection for backward compat
        collectionService.add(userId, recipeId, RecipeCollectionType.DONE);

        return toDTO(status, userId);
    }

    /**
     * Cancel a recipe that was IN_PROGRESS.
     * Removes its ingredient contributions from the shopping list.
     */
    @Transactional
    public void cancelInProgress(Long userId, Long recipeId) {
        statusRepository.findByUserAndRecipe(userId, recipeId)
            .ifPresent(s -> {
                statusRepository.delete(s);
                shoppingListService.removeRecipeIngredientsFromList(userId, recipeId);
            });
    }

    /**
     * Returns the status + feedback for a single recipe (used on recipe detail page).
     * Returns a DTO with status = null if the user has no status record for this recipe.
     */
    public UserRecipeStatusDTO getStatusForRecipe(Long userId, Long recipeId) {
        Optional<UserRecipeStatus> statusOpt = statusRepository.findByUserAndRecipe(userId, recipeId);
        if (statusOpt.isEmpty()) {
            UserRecipeStatusDTO dto = new UserRecipeStatusDTO();
            dto.recipeId = recipeId;
            dto.status = null;
            dto.feedback = getFeedbackDTO(userId, recipeId);
            return dto;
        }
        return toDTO(statusOpt.get(), userId);
    }

    /**
     * Returns all IN_PROGRESS recipes for the user, with full recipe data embedded.
     */
    public List<UserRecipeStatusDTO> getInProgress(Long userId) {
        return statusRepository.findByUserAndStatus(userId, RecipeStatus.IN_PROGRESS)
            .stream()
            .map(s -> toDTO(s, userId))
            .collect(Collectors.toList());
    }

    /**
     * Returns all DONE recipes for the user, with full recipe data embedded.
     */
    public List<UserRecipeStatusDTO> getDone(Long userId) {
        return statusRepository.findByUserAndStatus(userId, RecipeStatus.DONE)
            .stream()
            .map(s -> toDTO(s, userId))
            .collect(Collectors.toList());
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private UserRecipeStatusDTO toDTO(UserRecipeStatus status, Long userId) {
        UserRecipeStatusDTO dto = new UserRecipeStatusDTO();
        dto.recipeId = status.recipe.id;
        dto.status = status.status.name();
        dto.startedAt = status.startedAt;
        dto.completedAt = status.completedAt;
        dto.feedback = getFeedbackDTO(userId, status.recipe.id);
        dto.recipe = status.recipe.toRecipeDTO();
        dto.cookCount = status.cookCount;
        return dto;
    }

    private RecipeFeedbackDTO getFeedbackDTO(Long userId, Long recipeId) {
        return feedbackRepository.findByUserAndRecipe(userId, recipeId)
            .map(fb -> new RecipeFeedbackDTO(fb.rating, fb.likedNotes, fb.improvementNotes, fb.updatedAt))
            .orElse(null);
    }
}
