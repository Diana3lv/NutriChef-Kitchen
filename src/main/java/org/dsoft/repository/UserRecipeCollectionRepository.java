package org.dsoft.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.dsoft.entity.model.RecipeCollectionType;
import org.dsoft.entity.model.UserRecipeCollection;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserRecipeCollectionRepository implements PanacheRepository<UserRecipeCollection> {

    public List<UserRecipeCollection> findByUserAndType(Long userId, RecipeCollectionType type) {
        return find("user.id = ?1 AND collectionType = ?2", userId, type).list();
    }

    public Optional<UserRecipeCollection> findByUserRecipeAndType(Long userId, Long recipeId, RecipeCollectionType type) {
        return find("user.id = ?1 AND recipe.id = ?2 AND collectionType = ?3", userId, recipeId, type).firstResultOptional();
    }

    public boolean existsByUserRecipeAndType(Long userId, Long recipeId, RecipeCollectionType type) {
        return count("user.id = ?1 AND recipe.id = ?2 AND collectionType = ?3", userId, recipeId, type) > 0;
    }

    public List<Long> findRecipeIdsByUserAndType(Long userId, RecipeCollectionType type) {
        return find("user.id = ?1 AND collectionType = ?2", userId, type)
                .stream()
                .map(c -> c.recipe.id)
                .toList();
    }
}