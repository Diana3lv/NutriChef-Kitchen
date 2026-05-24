package org.dsoft.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import org.dsoft.entity.model.Ingredient;

@ApplicationScoped
public class IngredientRepository implements PanacheRepository<Ingredient> {

    public Optional<Ingredient> findByName(String name) {
        return find("lower(name) = lower(?1)", name).firstResultOptional();
    }
}
