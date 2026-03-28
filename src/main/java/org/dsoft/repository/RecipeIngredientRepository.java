package org.dsoft.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.dsoft.entity.model.Recipe;
import org.dsoft.entity.model.RecipeIngredient;

@ApplicationScoped
public class RecipeIngredientRepository implements PanacheRepository<RecipeIngredient> {

    public List<RecipeIngredient> findByRecipe(Recipe recipe) {
        return list("recipe", recipe);
    }

    public void deleteByRecipe(Recipe recipe) {
        delete("recipe", recipe);
    }
}
