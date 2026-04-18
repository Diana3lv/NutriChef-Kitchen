package org.dsoft.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dsoft.entity.dto.IngredientDTO;
import org.dsoft.entity.model.Allergen;
import org.dsoft.entity.model.Ingredient;
import org.dsoft.entity.model.IngredientSubstitution;
import org.dsoft.repository.IngredientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class IngredientService {

    private static final Logger logger = LoggerFactory.getLogger(IngredientService.class);

    @Inject
    IngredientRepository ingredientRepository;

    @Inject
    SubstitutionLLMService substitutionLLMService;

    public List<Ingredient> getAll() {
        return ingredientRepository.listAll();
    }

    public Optional<Ingredient> getById(Long id) {
        return ingredientRepository.findByIdOptional(id);
    }

    public Optional<Ingredient> findByName(String name) {
        return ingredientRepository.findByName(name);
    }

    @Transactional
    public Ingredient create(Ingredient ingredient) {
        ingredientRepository.persist(ingredient);
        populateSubstitutionsForIngredient(ingredient);
        return ingredient;
    }

    private void populateSubstitutionsForIngredient(Ingredient ingredient) {
        if (ingredient == null || ingredient.name == null || ingredient.name.isBlank()) {
            return;
        }

        try {
            List<IngredientDTO> substitutionDTOs = substitutionLLMService.findSubstitutions(ingredient.name);
            List<IngredientSubstitution> substitutions = convertDTOsToSubstitutions(ingredient, substitutionDTOs);
            ingredient.substitutions = substitutions;
            logger.info("Populated {} substitutions for ingredient '{}'", substitutions.size(), ingredient.name);
        } catch (Exception e) {
            logger.warn("Failed to populate substitutions for ingredient '{}': {}", ingredient.name, e.getMessage());
        }
    }

    private List<IngredientSubstitution> convertDTOsToSubstitutions(Ingredient original, List<IngredientDTO> dtos) {
        List<IngredientSubstitution> substitutions = new ArrayList<>();

        for (IngredientDTO dto : dtos) {
            List<String> validatedAllergens = validateAndEnrichAllergens(dto.getAllergens());
            
            Ingredient substitutionIngredient = ingredientRepository.findByName(dto.getName())
                .orElseGet(() -> {
                    Ingredient newIng = new Ingredient();
                    newIng.name = dto.getName();
                    newIng.unit = dto.getUnit();
                    newIng.allergens = convertAllergenStringsToEnum(validatedAllergens);
                    ingredientRepository.persist(newIng);
                    return newIng;
                });

            IngredientSubstitution sub = new IngredientSubstitution();
            sub.originalIngredient = original;
            sub.originalQuantity = 1.0;
            sub.originalUnit = original.unit;
            sub.ingredients.add(substitutionIngredient);
            sub.ratios.add(dto.getRatio() != null ? dto.getRatio() : "1:1");
            sub.persist();
            substitutions.add(sub);
        }

        return substitutions;
    }

    private List<String> validateAndEnrichAllergens(List<String> allergenStrings) {
        if (allergenStrings == null || allergenStrings.isEmpty()) {
            return new ArrayList<>();
        }

        return allergenStrings.stream()
                .filter(allergen -> {
                    try {
                        Allergen.valueOf(allergen.toUpperCase());
                        return true;
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid allergen value ignored: {}", allergen);
                        return false;
                    }
                })
                .map(String::toUpperCase)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Allergen> convertAllergenStringsToEnum(List<String> allergenStrings) {
        return allergenStrings.stream()
                .map(a -> {
                    try {
                        return Allergen.valueOf(a.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        logger.warn("Failed to convert allergen: {}", a);
                        return null;
                    }
                })
                .filter(a -> a != null)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<Ingredient> update(Long id, Ingredient ingredient) {
        return ingredientRepository.findByIdOptional(id)
            .map(entity -> {
                entity.name = ingredient.name;
                entity.unit = ingredient.unit;
                entity.allergens = ingredient.allergens;
                ingredientRepository.persist(entity);
                return entity;
            });
    }

    @Transactional
    public boolean delete(Long id) {
        return ingredientRepository.deleteById(id);
    }
}
