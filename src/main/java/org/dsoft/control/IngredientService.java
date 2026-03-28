package org.dsoft.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.dsoft.entity.model.Ingredient;
import org.dsoft.repository.IngredientRepository;

@ApplicationScoped
public class IngredientService {

    @Inject
    IngredientRepository repository;

    public List<Ingredient> getAll() {
        return repository.listAll();
    }

    public Ingredient getById(Long id) {
        return repository.findByIdOptional(id).orElse(null);
    }

    public Optional<Ingredient> findByName(String name) {
        return repository.findByName(name);
    }

    @Transactional
    public Ingredient create(Ingredient ingredient) {
        repository.persist(ingredient);
        return ingredient;
    }

    @Transactional
    public Ingredient update(Long id, Ingredient ingredient) {
        Ingredient entity = repository.findByIdOptional(id).orElse(null);
        if (entity == null) {
            return null;
        }
        entity.name = ingredient.name;
        entity.unit = ingredient.unit;
        entity.allergens = ingredient.allergens;
        repository.persist(entity);
        return entity;
    }

    @Transactional
    public boolean delete(Long id) {
        return repository.deleteById(id);
    }
}
