package org.dsoft.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.dsoft.entity.model.Recipe;

@ApplicationScoped
public class RecipeRepository implements PanacheRepository<Recipe> {

}
