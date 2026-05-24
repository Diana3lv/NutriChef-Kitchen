package org.dsoft.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import org.dsoft.entity.model.RecipeStatus;
import org.dsoft.entity.model.UserRecipeStatus;

@ApplicationScoped
public class UserRecipeStatusRepository implements PanacheRepository<UserRecipeStatus> {

    public Optional<UserRecipeStatus> findByUserAndRecipe(Long userId, Long recipeId) {
        return find("user.id = ?1 and recipe.id = ?2", userId, recipeId).firstResultOptional();
    }

    public List<UserRecipeStatus> findByUserAndStatus(Long userId, RecipeStatus status) {
        return find("user.id = ?1 and status = ?2", userId, status).list();
    }
}
