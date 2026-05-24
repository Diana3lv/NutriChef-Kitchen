package org.dsoft.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import org.dsoft.entity.model.ShoppingListItem;

@ApplicationScoped
public class ShoppingListItemRepository implements PanacheRepository<ShoppingListItem> {

    public List<ShoppingListItem> findByUser(Long userId) {
        return find("user.id", userId).list();
    }

    public List<ShoppingListItem> findByUserNotPurchased(Long userId) {
        return find("user.id = ?1 AND isPurchased = false", userId).list();
    }

    public Optional<ShoppingListItem> findByUserAndIngredient(Long userId, Long ingredientId) {
        return find("user.id = ?1 AND ingredient.id = ?2", userId, ingredientId).firstResultOptional();
    }

    public boolean existsByUserAndIngredient(Long userId, Long ingredientId) {
        return count("user.id = ?1 AND ingredient.id = ?2", userId, ingredientId) > 0;
    }

    public void deleteByUserAndIngredient(Long userId, Long ingredientId) {
        delete("user.id = ?1 AND ingredient.id = ?2", userId, ingredientId);
    }

    public void deleteAllByUser(Long userId) {
        delete("user.id", userId);
    }

    public void markAsPurchased(Long itemId) {
        find("id", itemId).firstResultOptional().ifPresent(item -> {
            item.isPurchased = true;
            persist(item);
        });
    }
}
