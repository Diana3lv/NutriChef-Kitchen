package org.dsoft.control;

import org.dsoft.entity.model.InventoryIngredient;
import org.dsoft.entity.model.Inventory;
import org.dsoft.entity.model.Ingredient;
import org.dsoft.entity.model.User;
import org.dsoft.entity.dto.InventoryIngredientDTO;
import org.dsoft.entity.dto.IngredientDTO;
import org.dsoft.repository.InventoryIngredientRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class InventoryService {

    @Inject
    InventoryIngredientRepository inventoryIngredientRepository;

    @Inject
    IngredientService ingredientService;

    public List<InventoryIngredientDTO> getUserInventory(Long userId) {
        return inventoryIngredientRepository.findByUserId(userId)
                .stream()
                .map(this::toDTO)
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

        // Get or create inventory for user
        Inventory inventory = user.inventory;
        if (inventory == null) {
            inventory = new Inventory();
            inventory.user = user;
            user.inventory = inventory;
            user.persist();
        }

        Ingredient ingredient = ingredientService.findByName(itemDTO.getIngredient().getName())
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found: " + itemDTO.getIngredient().getName()));

        InventoryIngredient inventoryIngredient = new InventoryIngredient();
        inventoryIngredient.inventory = inventory;
        inventoryIngredient.ingredient = ingredient;
        inventoryIngredient.quantity = itemDTO.getQuantity();
        inventoryIngredient.expiryDate = itemDTO.getExpiryDate();
        inventoryIngredient.notes = itemDTO.getNotes();

        inventoryIngredientRepository.persist(inventoryIngredient);

        return toDTO(inventoryIngredient);
    }

    @Transactional
    public void removeItem(Long itemId, Long userId) {
        inventoryIngredientRepository.findByIdAndUserId(itemId, userId)
                .ifPresent(inventoryIngredientRepository::delete);
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
        return toDTO(invIng);
    }

    @Transactional
    public int clearExpiredItems(Long userId) {
        return (int) inventoryIngredientRepository.deleteExpiredByUserId(userId, LocalDate.now());
    }

    public boolean hasIngredient(Long userId, String ingredientName) {
        return inventoryIngredientRepository.countByUserIdAndIngredientName(userId, ingredientName) > 0;
    }

    private InventoryIngredientDTO toDTO(InventoryIngredient invIng) {
        IngredientDTO ingDTO = new IngredientDTO(
                invIng.ingredient.name,
                invIng.ingredient.unit,
                invIng.ingredient.allergens.stream().map(Enum::name).collect(Collectors.toList()),
                null  // ratio field is for substitutions only, not used in inventory
        );

        return new InventoryIngredientDTO(
                ingDTO,
                invIng.quantity,
                invIng.expiryDate,
                invIng.notes,
                invIng.dateAdded
        );
    }
}
