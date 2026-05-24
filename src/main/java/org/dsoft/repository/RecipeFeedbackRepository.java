package org.dsoft.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import org.dsoft.entity.model.RecipeFeedback;

@ApplicationScoped
public class RecipeFeedbackRepository implements PanacheRepository<RecipeFeedback> {

    public Optional<RecipeFeedback> findByUserAndRecipe(Long userId, Long recipeId) {
        return find("user.id = ?1 and recipe.id = ?2", userId, recipeId).firstResultOptional();
    }
}
