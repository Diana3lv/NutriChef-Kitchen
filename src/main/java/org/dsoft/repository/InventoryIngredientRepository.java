package org.dsoft.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.dsoft.entity.model.InventoryIngredient;

@ApplicationScoped
public class InventoryIngredientRepository implements PanacheRepository<InventoryIngredient> {

    public List<InventoryIngredient> findByUserId(Long userId) {
        return list("inventory.user.id", Sort.by("dateAdded").descending(), userId);
    }

    public Optional<InventoryIngredient> findByIdAndUserId(Long id, Long userId) {
        return find("id = ?1 and inventory.user.id = ?2", id, userId).firstResultOptional();
    }

    public long deleteExpiredByUserId(Long userId, LocalDate today) {
        return delete("inventory.user.id = ?1 and expiryDate < ?2", userId, today);
    }

    public long countByUserIdAndIngredientName(Long userId, String ingredientName) {
        return count("inventory.user.id = ?1 and lower(ingredient.name) = lower(?2)", userId, ingredientName);
    }
}
