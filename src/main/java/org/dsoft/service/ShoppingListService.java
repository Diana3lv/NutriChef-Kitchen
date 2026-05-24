package org.dsoft.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dsoft.entity.dto.ShoppingListItemDTO;
import org.dsoft.entity.model.Ingredient;
import org.dsoft.entity.model.InventoryIngredient;
import org.dsoft.entity.model.Recipe;
import org.dsoft.entity.model.RecipeIngredient;
import org.dsoft.entity.model.RecipeStatus;
import org.dsoft.entity.model.ShoppingListItem;
import org.dsoft.entity.model.User;
import org.dsoft.entity.model.UserRecipeStatus;
import org.dsoft.repository.IngredientRepository;
import org.dsoft.repository.InventoryIngredientRepository;
import org.dsoft.repository.RecipeRepository;
import org.dsoft.repository.ShoppingListItemRepository;
import org.dsoft.repository.UserRepository;
import org.dsoft.control.InventoryService;
import org.dsoft.repository.UserRecipeStatusRepository;

@ApplicationScoped
public class ShoppingListService {

    @Inject
    ShoppingListItemRepository shoppingListItemRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    RecipeRepository recipeRepository;

    @Inject
    IngredientRepository ingredientRepository;

    @Inject
    InventoryIngredientRepository inventoryIngredientRepository;

    @Inject
    InventoryService inventoryService;

    @Inject
    UserRecipeStatusRepository userRecipeStatusRepository;

    /**
     * Get all shopping list items for a user (not yet purchased)
     */
    @Transactional
    public List<ShoppingListItemDTO> getShoppingList(Long userId) {
        List<ShoppingListItem> items = shoppingListItemRepository.findByUserNotPurchased(userId);
        return items.stream()
            .filter(item -> item.quantity == null || item.quantity.compareTo(BigDecimal.ZERO) > 0)
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Add all ingredients from a recipe to shopping list.
     * - Subtracts quantities the user already has in inventory.
     * - If an item is already in the list (not purchased), accumulates the needed qty.
     * - If an item was previously purchased, resets it so the user can buy it again.
     */
    @Transactional
    public void addRecipeIngredientsToList(Long userId, Long recipeId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        Recipe recipe = recipeRepository.findById(recipeId);
        if (recipe == null) {
            throw new IllegalArgumentException("Recipe not found");
        }

        // Build inventory map using the repository so we get fresh data and correctly
        // accumulate multiple rows for the same ingredient (e.g. two separate pantry entries).
        Map<Long, BigDecimal> inventoryMap = new HashMap<>();
        for (InventoryIngredient invItem : inventoryIngredientRepository.findByUserId(userId)) {
            inventoryMap.merge(invItem.ingredient.id, parseQuantity(invItem.quantity), BigDecimal::add);
        }

        for (var recipeIngredient : recipe.recipeIngredients) {
            Long ingredientId = recipeIngredient.ingredient.id;
            BigDecimal recipeQty = parseQuantity(recipeIngredient.quantity);

            // How much still needs to be bought after subtracting what is in inventory
            BigDecimal inventoryQty = inventoryMap.getOrDefault(ingredientId, BigDecimal.ZERO);
            BigDecimal neededQty = recipeQty.subtract(inventoryQty).max(BigDecimal.ZERO);

            Optional<ShoppingListItem> existing =
                shoppingListItemRepository.findByUserAndIngredient(userId, ingredientId);

            if (existing.isPresent()) {
                ShoppingListItem item = existing.get();
                if (item.isPurchased) {
                    // Previously bought - reset with just this recipe's needs
                    item.baseQuantity = recipeQty;
                    item.quantity = neededQty;
                    item.isPurchased = false;
                } else {
                    // Accumulate: reconstruct previous base from stored quantity + current inventory,
                    // then add this recipe's full requirement. This avoids double-subtracting inventory.
                    BigDecimal prevBase = item.baseQuantity != null
                        ? item.baseQuantity
                        : item.quantity.add(inventoryQty);
                    item.baseQuantity = prevBase.add(recipeQty);
                    item.quantity = item.baseQuantity.subtract(inventoryQty).max(BigDecimal.ZERO);
                }
                item.notes = "From recipe: " + recipe.title;
                shoppingListItemRepository.persist(item);
            } else {
                // Always create a ghost record, even if neededQty = 0 (fully covered by inventory).
                // This ensures the item reappears automatically if inventory later drops,
                // and correctly tracks all ingredients across all in-progress recipes.
                ShoppingListItem item = new ShoppingListItem();
                item.user = user;
                item.ingredient = recipeIngredient.ingredient;
                item.baseQuantity = recipeQty;
                item.quantity = neededQty; // may be 0 if fully covered
                item.unit = recipeIngredient.ingredient.unit;
                item.isPurchased = false;
                item.notes = "From recipe: " + recipe.title;
                shoppingListItemRepository.persist(item);
            }
        }
    }

    /**
     * Safely parse a quantity string (e.g. "200", "1.5", "2 cups") into a BigDecimal.
     * Falls back to 1 if the string is not parseable.
     */
    private BigDecimal parseQuantity(String quantityStr) {
        if (quantityStr == null || quantityStr.isBlank()) {
            return BigDecimal.ONE;
        }
        try {
            return new BigDecimal(quantityStr.trim());
        } catch (NumberFormatException e) {
            // Extract the leading numeric portion (e.g. "2 cups" → "2", "1.5g" → "1.5")
            StringBuilder numStr = new StringBuilder();
            boolean hasDecimalPoint = false;
            for (char c : quantityStr.trim().toCharArray()) {
                if (Character.isDigit(c)) {
                    numStr.append(c);
                } else if (c == '.' && !hasDecimalPoint) {
                    numStr.append(c);
                    hasDecimalPoint = true;
                } else if (numStr.length() > 0) {
                    break;
                }
            }
            try {
                return numStr.length() > 0 ? new BigDecimal(numStr.toString()) : BigDecimal.ONE;
            } catch (NumberFormatException e2) {
                return BigDecimal.ONE;
            }
        }
    }

    @Transactional
    public ShoppingListItemDTO addItem(
        Long userId,
        Long ingredientId,
        BigDecimal quantity,
        String notes
    ) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        Ingredient ingredient = ingredientRepository.findById(ingredientId);
        if (ingredient == null) {
            throw new IllegalArgumentException("Ingredient not found");
        }

        var existing = shoppingListItemRepository.findByUserAndIngredient(userId, ingredientId);
        if (existing.isPresent()) {
            ShoppingListItem item = existing.get();
            BigDecimal prevOwn = item.ownQuantity != null ? item.ownQuantity : BigDecimal.ZERO;
            item.ownQuantity = prevOwn.add(quantity);
            BigDecimal recipeNeeded = BigDecimal.ZERO;
            if (item.baseQuantity != null) {
                BigDecimal invQty = getInventoryQtyForIngredient(userId, ingredientId);
                recipeNeeded = item.baseQuantity.subtract(invQty).max(BigDecimal.ZERO);
            }
            item.quantity = recipeNeeded.add(item.ownQuantity);
            item.notes = notes;
            shoppingListItemRepository.persist(item);
            return mapToDTO(item);
        }

        ShoppingListItem item = new ShoppingListItem();
        item.user = user;
        item.ingredient = ingredient;
        item.ownQuantity = quantity;
        item.quantity = quantity;
        item.baseQuantity = null;
        item.unit = ingredient.unit;
        item.isPurchased = false;
        item.notes = notes;
        shoppingListItemRepository.persist(item);

        return mapToDTO(item);
    }

    /**
     * Removes a recipe's contribution from the shopping list.
     * Reduces each item's baseQuantity by the recipe's ingredient amount.
     * Items whose baseQuantity reaches zero are zeroed out (and hidden by the GET filter).
     * Use this when the user "un-plans" a recipe they previously added.
     */
    @Transactional
    public void removeRecipeIngredientsFromList(Long userId, Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId);
        if (recipe == null || recipe.recipeIngredients == null) return;

        Map<Long, BigDecimal> inventoryMap = new HashMap<>();
        for (InventoryIngredient invItem : inventoryIngredientRepository.findByUserId(userId)) {
            inventoryMap.merge(invItem.ingredient.id, parseQuantity(invItem.quantity), BigDecimal::add);
        }

        for (RecipeIngredient recipeIngredient : recipe.recipeIngredients) {
            Long ingredientId = recipeIngredient.ingredient.id;
            BigDecimal recipeQty = parseQuantity(recipeIngredient.quantity);

            shoppingListItemRepository.findByUserAndIngredient(userId, ingredientId)
                .ifPresent(item -> {
                    if (item.baseQuantity == null) return; // Manually-added item - leave it alone

                    BigDecimal newBase = item.baseQuantity.subtract(recipeQty).max(BigDecimal.ZERO);
                    item.baseQuantity = newBase;

                    if (newBase.compareTo(BigDecimal.ZERO) == 0) {
                        // No recipes left that need this ingredient
                        item.quantity = BigDecimal.ZERO;
                    } else {
                        // Recalculate from updated base minus current inventory
                        BigDecimal inventoryQty = inventoryMap.getOrDefault(ingredientId, BigDecimal.ZERO);
                        item.quantity = newBase.subtract(inventoryQty).max(BigDecimal.ZERO);
                    }
                    shoppingListItemRepository.persist(item);
                });
        }
    }

    @Transactional
    public void removeItem(Long userId, Long ingredientId) {
        shoppingListItemRepository.findByUserAndIngredient(userId, ingredientId)
            .ifPresent(item -> {
                if (item.baseQuantity != null) {
                    // Recipe-sourced: zero it out so it can reappear if needed later
                    item.quantity = BigDecimal.ZERO;
                    item.baseQuantity = BigDecimal.ZERO;
                    shoppingListItemRepository.persist(item);
                } else {
                    // Manually-added: full delete is fine
                    shoppingListItemRepository.delete(item);
                }
            });
    }

    /**
     * Called when user physically picks up a shopping list item (taps "Got it" while shopping).
     * Adds the purchased quantity directly to the user's inventory, then re-syncs the list.
     * The item's calculated quantity drops to 0 and it is hidden from the next GET.
     */
    @Transactional
    public void markBought(Long userId, Long itemId) {
        ShoppingListItem item = shoppingListItemRepository.findById(itemId);
        if (item == null || !item.user.id.equals(userId)) return;
        if (item.quantity == null || item.quantity.compareTo(BigDecimal.ZERO) <= 0) return;

        // Move purchased quantity straight into the pantry
        inventoryService.addOrIncrease(userId, item.ingredient.id, item.quantity);

        // Clear own quantity - the user bought everything they wanted
        item.ownQuantity = BigDecimal.ZERO;
        shoppingListItemRepository.persist(item);

        // Re-sync: recipe portion is now fully covered → quantity becomes 0 → hidden
        syncWithInventory(userId);
    }

    @Transactional
    public void clearPurchased(Long userId) {
        // Legacy: remove any leftover items with isPurchased=true from the old model
        shoppingListItemRepository.findByUser(userId).stream()
            .filter(item -> item.isPurchased)
            .forEach(shoppingListItemRepository::delete);
    }

    /**
     * When a recipe is marked as done (cooked), reduces the baseQuantity of matching shopping list
     * items by that recipe's ingredient amounts. Purchased items are also un-marked so that
     * syncWithInventory can re-evaluate whether anything still needs to be bought.
     */
    @Transactional
    public void reduceBaseQuantityForCookedRecipe(Long userId, Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId);
        if (recipe == null || recipe.recipeIngredients == null) return;

        for (RecipeIngredient recipeIngredient : recipe.recipeIngredients) {
            Long ingredientId = recipeIngredient.ingredient.id;
            BigDecimal recipeQty = parseQuantity(recipeIngredient.quantity);

            shoppingListItemRepository.findByUserAndIngredient(userId, ingredientId)
                .ifPresent(item -> {
                    if (item.baseQuantity == null) return; // Manually added - skip

                    BigDecimal newBase = item.baseQuantity.subtract(recipeQty).max(BigDecimal.ZERO);
                    item.baseQuantity = newBase;

                    if (item.isPurchased) {
                        // Un-purchase so syncWithInventory can re-evaluate remaining need
                        item.isPurchased = false;
                    }
                    if (newBase.compareTo(BigDecimal.ZERO) == 0) {
                        item.quantity = BigDecimal.ZERO; // No recipes left need this - hide it
                    }
                    shoppingListItemRepository.persist(item);
                });
        }
    }

    /**
     * Recalculates quantities for all recipe-sourced (non-purchased) shopping list items
     * based on the user's current inventory. Items whose baseQuantity is fully covered
     * by inventory are removed automatically.
     * Manually-added items (baseQuantity == null) are never touched.
     */
    @Transactional
    public void syncWithInventory(Long userId) {
        // Use findByUser (not findByUserNotPurchased) so legacy rows with isPurchased=true
        // also get re-evaluated. In the current model isPurchased is never set, but old data
        // might have it and would otherwise be stuck hidden forever.
        List<ShoppingListItem> items = shoppingListItemRepository.findByUser(userId)
            .stream()
            .filter(i -> i.baseQuantity != null)
            .collect(Collectors.toList());

        if (items.isEmpty()) return;

        // Build current inventory map: ingredientId → total quantity
        Map<Long, BigDecimal> inventoryMap = new HashMap<>();
        for (InventoryIngredient invItem : inventoryIngredientRepository.findByUserId(userId)) {
            inventoryMap.merge(
                invItem.ingredient.id,
                parseQuantity(invItem.quantity),
                BigDecimal::add
            );
        }

        for (ShoppingListItem item : items) {
            BigDecimal inventoryQty = inventoryMap.getOrDefault(item.ingredient.id, BigDecimal.ZERO);
            BigDecimal recipeNeeded = item.baseQuantity.subtract(inventoryQty).max(BigDecimal.ZERO);
            BigDecimal ownQty = item.ownQuantity != null ? item.ownQuantity : BigDecimal.ZERO;
            // displayed = max(0, recipes_needed - inventory) + own_qty
            item.quantity = recipeNeeded.add(ownQty);
            shoppingListItemRepository.persist(item);
        }
    }

    private ShoppingListItemDTO mapToDTO(ShoppingListItem item) {
        return new ShoppingListItemDTO(
            item.id,
            item.ingredient.id,
            item.ingredient.name,
            item.ingredient.category,
            item.quantity,
            item.unit,
            item.isPurchased,
            item.isChecked != null ? item.isChecked : false,
            item.dateAdded,
            item.notes,
            item.ownQuantity != null ? item.ownQuantity : BigDecimal.ZERO
        );
    }

    /**
     * Returns the total inventory quantity for a single ingredient for a user.
     */
    private BigDecimal getInventoryQtyForIngredient(Long userId, Long ingredientId) {
        return inventoryIngredientRepository.findByUserId(userId).stream()
            .filter(inv -> inv.ingredient.id.equals(ingredientId))
            .map(inv -> parseQuantity(inv.quantity))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public ShoppingListItemDTO updateItemQuantity(Long userId, Long itemId, BigDecimal newQty) {
        ShoppingListItem item = shoppingListItemRepository.findById(itemId);
        if (item == null || !item.user.id.equals(userId)) {
            throw new IllegalArgumentException("Item not found");
        }
        item.quantity = newQty.max(BigDecimal.ZERO);
        shoppingListItemRepository.persist(item);
        return mapToDTO(item);
    }

    /**
     * Validate and remove a set of items by ingredientId.
     * Returns a non-null error message if any item is needed by an in-progress recipe
     * and the inventory does not fully cover that recipe's requirement.
     * Returns null if removal is safe; also performs the removal.
     */
    @Transactional
    public String validateAndRemoveItems(Long userId, List<Long> ingredientIds) {
        // Collect all in-progress recipes
        List<UserRecipeStatus> inProgressStatuses =
            userRecipeStatusRepository.findByUserAndStatus(userId, RecipeStatus.IN_PROGRESS);

        List<String> blockedBy = new java.util.ArrayList<>();

        // Block deletion of any ingredient that appears in an in-progress recipe
        for (Long ingredientId : ingredientIds) {
            for (UserRecipeStatus urs : inProgressStatuses) {
                if (urs.recipe == null || urs.recipe.recipeIngredients == null) continue;
                for (RecipeIngredient ri : urs.recipe.recipeIngredients) {
                    if (ri.ingredient.id.equals(ingredientId)) {
                        blockedBy.add(urs.recipe.title);
                        break; // Move to next recipe
                    }
                }
            }
        }

        if (!blockedBy.isEmpty()) {
            String recipeList = blockedBy.stream().distinct().collect(Collectors.joining(", "));
            return "Cannot remove: ingredient needed by in-progress recipe(s): " + recipeList;
        }

        // Safe to remove
        for (Long ingredientId : ingredientIds) {
            removeItem(userId, ingredientId);
        }
        return null;
    }

    @Transactional
    public ShoppingListItemDTO toggleChecked(Long userId, Long itemId) {
        ShoppingListItem item = shoppingListItemRepository.findById(itemId);
        if (item == null || !item.user.id.equals(userId)) {
            throw new IllegalArgumentException("Item not found");
        }
        item.isChecked = !item.isChecked;
        shoppingListItemRepository.persist(item);
        return mapToDTO(item);
    }

    @Transactional
    public ShoppingListItemDTO reduceOwnQuantity(Long userId, Long itemId, BigDecimal amount) {
        ShoppingListItem item = shoppingListItemRepository.findById(itemId);
        if (item == null || !item.user.id.equals(userId)) {
            throw new IllegalArgumentException("Item not found");
        }

        BigDecimal currentOwn = item.ownQuantity != null ? item.ownQuantity : BigDecimal.ZERO;
        if (amount.compareTo(BigDecimal.ONE) < 0 || amount.compareTo(currentOwn) > 0) {
            throw new IllegalArgumentException(
                "Amount must be between 1 and " + currentOwn);
        }

        item.ownQuantity = currentOwn.subtract(amount);

        // Purely manual item with no recipe portion - delete the row
        if (item.ownQuantity.compareTo(BigDecimal.ZERO) == 0 && item.baseQuantity == null) {
            shoppingListItemRepository.delete(item);
            return null;
        }

        // Recalculate displayed quantity: max(0, baseQty - inventory) + ownQty
        BigDecimal recipeNeeded = BigDecimal.ZERO;
        if (item.baseQuantity != null) {
            BigDecimal invQty = getInventoryQtyForIngredient(userId, item.ingredient.id);
            recipeNeeded = item.baseQuantity.subtract(invQty).max(BigDecimal.ZERO);
        }
        item.quantity = recipeNeeded.add(item.ownQuantity);
        shoppingListItemRepository.persist(item);
        return mapToDTO(item);
    }
}
