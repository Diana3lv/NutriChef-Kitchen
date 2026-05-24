package org.dsoft.control;

import org.dsoft.entity.model.InventoryIngredient;
import org.dsoft.entity.model.Inventory;
import org.dsoft.entity.model.Ingredient;
import org.dsoft.entity.model.Recipe;
import org.dsoft.entity.model.RecipeIngredient;
import org.dsoft.entity.model.User;
import org.dsoft.entity.dto.InventoryIngredientDTO;
import org.dsoft.entity.dto.IngredientDTO;
import org.dsoft.repository.InventoryIngredientRepository;
import org.dsoft.repository.IngredientRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class InventoryService {

    public static class MissingIngredientInfo {
        public String name;
        public String needed;
        public String available;
        public String unit;

        public MissingIngredientInfo(String name, String needed, String available, String unit) {
            this.name = name;
            this.needed = needed;
            this.available = available;
            this.unit = unit;
        }
    }

    @Inject
    InventoryIngredientRepository inventoryIngredientRepository;

    @Inject
    IngredientRepository ingredientRepository;

    @Inject
    IngredientService ingredientService;

    public List<InventoryIngredientDTO> getUserInventory(Long userId) {
        return inventoryIngredientRepository.findByUserId(userId)
                .stream()
                .map(InventoryIngredient::toDTO)
                .collect(Collectors.toList());
    }

    public Set<String> getUserIngredientNames(Long userId) {
        return inventoryIngredientRepository.findByUserId(userId)
                .stream()
                .map(invIng -> invIng.ingredient.name.toLowerCase())
                .collect(Collectors.toSet());
    }

    @Transactional
    public InventoryIngredientDTO addItem(Long userId, InventoryIngredientDTO itemDTO) {
        User user = User.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        Ingredient ingredient = ingredientService.findByName(itemDTO.getIngredient().getName())
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found: " + itemDTO.getIngredient().getName()));

        List<InventoryIngredient> existing =
                inventoryIngredientRepository.findByUserIdAndIngredientId(userId, ingredient.id);
        if (!existing.isEmpty()) {
            InventoryIngredient inventoryIngredient = existing.get(0);
            BigDecimal current = parseQuantity(inventoryIngredient.quantity);
            inventoryIngredient.quantity = current.add(parseQuantity(itemDTO.getQuantity()))
                    .stripTrailingZeros().toPlainString();
            if (itemDTO.getExpiryDate() != null) inventoryIngredient.expiryDate = itemDTO.getExpiryDate();
            if (itemDTO.getNotes() != null && !itemDTO.getNotes().isBlank()) inventoryIngredient.notes = itemDTO.getNotes();
            inventoryIngredientRepository.persist(inventoryIngredient);
            return inventoryIngredient.toDTO();
        }

        Inventory inventory = ensureInventory(user);

        InventoryIngredient inventoryIngredient = new InventoryIngredient();
        inventoryIngredient.inventory = inventory;
        inventoryIngredient.ingredient = ingredient;
        inventoryIngredient.quantity = itemDTO.getQuantity();
        inventoryIngredient.expiryDate = itemDTO.getExpiryDate();
        inventoryIngredient.notes = itemDTO.getNotes();

        inventoryIngredientRepository.persist(inventoryIngredient);

        return inventoryIngredient.toDTO();
    }

    @Transactional
    public void removeItem(Long itemId, Long userId) {
        inventoryIngredientRepository.findByIdAndUserId(itemId, userId)
                .ifPresent(inventoryIngredientRepository::delete);
    }

    @Transactional
    public InventoryIngredientDTO addOrIncrease(Long userId, Long ingredientId, BigDecimal qty) {
        List<InventoryIngredient> existing =
                inventoryIngredientRepository.findByUserIdAndIngredientId(userId, ingredientId);

        if (!existing.isEmpty()) {
            InventoryIngredient item = existing.get(0);
            BigDecimal current = parseQuantity(item.quantity);
            item.quantity = current.add(qty).stripTrailingZeros().toPlainString();
            inventoryIngredientRepository.persist(item);
            return item.toDTO();
        }

        Ingredient ingredient = ingredientRepository.findById(ingredientId);
        if (ingredient == null) return null;

        User user = User.findById(userId);
        Inventory inventory = ensureInventory(user);

        InventoryIngredient item = new InventoryIngredient();
        item.inventory = inventory;
        item.ingredient = ingredient;
        item.quantity = qty.stripTrailingZeros().toPlainString();
        inventoryIngredientRepository.persist(item);
        return item.toDTO();
    }

    @Transactional
    public InventoryIngredientDTO addItemById(Long userId, Long ingredientId, String quantity,
                                              String notes, java.time.LocalDate expiryDate) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId);
        if (ingredient == null) {
            throw new IllegalArgumentException("Ingredient not found: " + ingredientId);
        }

        List<InventoryIngredient> existing =
                inventoryIngredientRepository.findByUserIdAndIngredientId(userId, ingredientId);

        if (!existing.isEmpty()) {
            InventoryIngredient item = existing.get(0);
            BigDecimal current = parseQuantity(item.quantity);
            item.quantity = current.add(parseQuantity(quantity)).stripTrailingZeros().toPlainString();
            if (notes != null && !notes.isBlank()) item.notes = notes;
            if (expiryDate != null) item.expiryDate = expiryDate;
            inventoryIngredientRepository.persist(item);
            return item.toDTO();
        }

        User user = User.findById(userId);
        Inventory inventory = ensureInventory(user);

        InventoryIngredient item = new InventoryIngredient();
        item.inventory = inventory;
        item.ingredient = ingredient;
        item.quantity = quantity;
        item.notes = notes;
        item.expiryDate = expiryDate;
        inventoryIngredientRepository.persist(item);
        return item.toDTO();
    }

    private Inventory ensureInventory(User user) {
        if (user.inventory != null) return user.inventory;
        Inventory inventory = new Inventory();
        inventory.user = user;
        user.inventory = inventory;
        inventory.persist();
        return inventory;
    }

    @Transactional
    public InventoryIngredientDTO updateItem(Long itemId, Long userId, InventoryIngredientDTO itemDTO) {
        var itemOpt = inventoryIngredientRepository.findByIdAndUserId(itemId, userId);

        if (itemOpt.isEmpty()) {
            return null;
        }

        InventoryIngredient invIng = itemOpt.get();

        invIng.quantity = itemDTO.getQuantity();
        invIng.expiryDate = itemDTO.getExpiryDate();
        invIng.notes = itemDTO.getNotes();

        inventoryIngredientRepository.persist(invIng);
        return invIng.toDTO();
    }

    @Transactional
    public int clearExpiredItems(Long userId) {
        return (int) inventoryIngredientRepository.deleteExpiredByUserId(userId, LocalDate.now());
    }

    public boolean hasIngredient(Long userId, String ingredientName) {
        return inventoryIngredientRepository.countByUserIdAndIngredientName(userId, ingredientName) > 0;
    }

    public List<MissingIngredientInfo> validateRecipeIngredientsAvailable(Long userId, Long recipeId) {
        Recipe recipe = Recipe.findById(recipeId);
        if (recipe == null || recipe.recipeIngredients == null || recipe.recipeIngredients.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // Group recipe ingredient quantities by ingredient ID
        Map<Long, BigDecimal> neededMap = new java.util.LinkedHashMap<>();
        for (RecipeIngredient recipeIngredient : recipe.recipeIngredients) {
            Long ingredientId = recipeIngredient.ingredient.id;
            BigDecimal qty = parseQuantity(recipeIngredient.quantity);
            neededMap.merge(ingredientId, qty, BigDecimal::add);
        }

        // Get user's inventory grouped by ingredient ID (sum across all rows per ingredient)
        List<InventoryIngredient> userInventory = inventoryIngredientRepository.findByUserId(userId);
        Map<Long, BigDecimal> availableMap = new java.util.LinkedHashMap<>();
        for (InventoryIngredient inv : userInventory) {
            Long ingredientId = inv.ingredient.id;
            BigDecimal qty = parseQuantity(inv.quantity);
            availableMap.merge(ingredientId, qty, BigDecimal::add);
        }

        // Build ingredient ID -> RecipeIngredient map to look up unit
        Map<Long, RecipeIngredient> recipeIngMap = new java.util.LinkedHashMap<>();
        for (RecipeIngredient ri : recipe.recipeIngredients) {
            recipeIngMap.putIfAbsent(ri.ingredient.id, ri);
        }

        // Check each required ingredient
        List<MissingIngredientInfo> missing = new java.util.ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : neededMap.entrySet()) {
            Long ingredientId = entry.getKey();
            BigDecimal neededQty = entry.getValue();
            BigDecimal availableQty = availableMap.getOrDefault(ingredientId, BigDecimal.ZERO);

            if (availableQty.compareTo(neededQty) < 0) {
                Ingredient ingredient = ingredientRepository.findById(ingredientId);
                if (ingredient != null) {
                    String unit = ingredient.unit != null ? ingredient.unit : "";
                    String neededStr = neededQty.stripTrailingZeros().toPlainString();
                    String availableStr = availableQty.stripTrailingZeros().toPlainString();
                    missing.add(new MissingIngredientInfo(ingredient.name, neededStr, availableStr, unit));
                }
            }
        }

        return missing;
    }

    /**
     * Subtracts the ingredients of a recipe from the user's inventory.
     * Recipe ingredients for the same ingredient ID are summed first to avoid
     * double-subtraction when a recipe lists the same ingredient in multiple entries.
     * Inventory rows that reach zero are removed entirely.
     * Ingredients not present in inventory are silently skipped.
     */
    @Transactional
    public void subtractRecipeIngredients(Long userId, Long recipeId) {
        Recipe recipe = Recipe.findById(recipeId);
        if (recipe == null || recipe.recipeIngredients == null) return;

        // Group recipe ingredient quantities by ingredient ID to avoid double-subtraction
        Map<Long, BigDecimal> toSubtractMap = new java.util.LinkedHashMap<>();
        for (RecipeIngredient recipeIngredient : recipe.recipeIngredients) {
            Long ingredientId = recipeIngredient.ingredient.id;
            BigDecimal qty = parseQuantity(recipeIngredient.quantity);
            toSubtractMap.merge(ingredientId, qty, BigDecimal::add);
        }

        for (Map.Entry<Long, BigDecimal> entry : toSubtractMap.entrySet()) {
            Long ingredientId = entry.getKey();
            BigDecimal toSubtract = entry.getValue();

            List<InventoryIngredient> invItems =
                inventoryIngredientRepository.findByUserIdAndIngredientId(userId, ingredientId);

            for (InventoryIngredient invItem : invItems) {
                if (toSubtract.compareTo(BigDecimal.ZERO) <= 0) break;
                BigDecimal invQty = parseQuantity(invItem.quantity);
                if (invQty.compareTo(toSubtract) <= 0) {
                    // Entire stock consumed
                    inventoryIngredientRepository.delete(invItem);
                    toSubtract = toSubtract.subtract(invQty);
                } else {
                    // Partial consumption (-=)
                    invItem.quantity = invQty.subtract(toSubtract).stripTrailingZeros().toPlainString();
                    inventoryIngredientRepository.persist(invItem);
                    toSubtract = BigDecimal.ZERO;
                }
            }
        }
    }

    private BigDecimal parseQuantity(String quantityStr) {
        if (quantityStr == null || quantityStr.isBlank()) {
            return BigDecimal.ONE;
        }
        try {
            return new BigDecimal(quantityStr.trim());
        } catch (NumberFormatException e) {
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
            if (numStr.length() == 0) return BigDecimal.ONE;
            try {
                return new BigDecimal(numStr.toString());
            } catch (NumberFormatException ex) {
                return BigDecimal.ONE;
            }
        }
    }
}
