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
    InventoryIngredientRepository repository;

    @Inject
    IngredientService ingredientService;

    public List<InventoryIngredientDTO> getUserInventory(Long userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Set<String> getUserIngredientNames(Long userId) {
        return repository.findByUserId(userId)
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

        repository.persist(inventoryIngredient);

        return toDTO(inventoryIngredient);
    }

    @Transactional
    public void removeItem(Long itemId, Long userId) {
        repository.findByIdAndUserId(itemId, userId)
                .ifPresent(repository::delete);
    }

    @Transactional
    public InventoryIngredientDTO updateItem(Long itemId, Long userId, InventoryIngredientDTO itemDTO) {
        var itemOpt = repository.findByIdAndUserId(itemId, userId);

        if (itemOpt.isEmpty()) {
            return null;
        }

        InventoryIngredient invIng = itemOpt.get();

        invIng.quantity = itemDTO.getQuantity();
        invIng.expiryDate = itemDTO.getExpiryDate();
        invIng.notes = itemDTO.getNotes();

        repository.persist(invIng);
        return toDTO(invIng);
    }

    @Transactional
    public int clearExpiredItems(Long userId) {
        return (int) repository.deleteExpiredByUserId(userId, LocalDate.now());
    }

    public boolean hasIngredient(Long userId, String ingredientName) {
        return repository.countByUserIdAndIngredientName(userId, ingredientName) > 0;
    }

    private InventoryIngredientDTO toDTO(InventoryIngredient invIng) {
        IngredientDTO ingDTO = new IngredientDTO(
                invIng.ingredient.id,
                invIng.ingredient.name,
                invIng.ingredient.unit,
                invIng.ingredient.allergens.stream().map(Enum::name).collect(Collectors.toList())
        );

        return new InventoryIngredientDTO(
                invIng.id,
                ingDTO,
                invIng.quantity,
                invIng.expiryDate,
                invIng.notes,
                invIng.dateAdded
        );
    }
}
