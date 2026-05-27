package org.dsoft.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dsoft.entity.dto.IngredientDTO;
import org.dsoft.entity.dto.IngredientWithSubstitutionsDTO;
import org.dsoft.entity.dto.SubstitutionAlternativeInputDTO;
import org.dsoft.entity.dto.SubstitutionAlternativeResponseDTO;
import org.dsoft.entity.dto.SubstitutionDTO;
import org.dsoft.entity.dto.SubstitutionOptionResponseDTO;
import org.dsoft.entity.dto.UpdateIngredientRequestDTO;
import org.dsoft.entity.model.Allergen;
import org.dsoft.entity.model.Ingredient;
import org.dsoft.entity.model.IngredientCategory;
import org.dsoft.entity.model.SubstitutionAlternative;
import org.dsoft.entity.model.SubstitutionOption;
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
        
        // Populate substitutions after persisting the ingredient
        if (ingredient != null && ingredient.name != null && !ingredient.name.isBlank()) {
            try {
                List<IngredientDTO> substitutionDTOs = substitutionLLMService.findSubstitutions(ingredient.name);
                List<SubstitutionOption> substitutions = convertDTOsToSubstitutionOptions(ingredient, substitutionDTOs);
                // Add to list instead of reassigning to avoid orphan deletion issues
                ingredient.substitutions.addAll(substitutions);
                logger.info("Populated {} substitutions for ingredient '{}'", substitutions.size(), ingredient.name);
            } catch (Exception e) {
                logger.warn("Failed to populate substitutions for ingredient '{}': {}", ingredient.name, e.getMessage());
            }
        }
        
        return ingredient;
    }

    private List<SubstitutionOption> convertDTOsToSubstitutionOptions(Ingredient original, List<IngredientDTO> dtos) {
        List<SubstitutionOption> substitutions = new ArrayList<>();

        for (IngredientDTO dto : dtos) {
            List<String> validatedAllergens = validateAndEnrichAllergens(dto.getAllergens());
            
            Ingredient substitutionIngredient = ingredientRepository.findByName(dto.getName())
                .orElseGet(() -> {
                    try {
                        Ingredient newIng = new Ingredient();
                        newIng.name = dto.getName();
                        newIng.unit = dto.getUnit();
                        newIng.category = IngredientCategory.fromDisplayValue(dto.getCategory());
                        newIng.allergens = convertAllergenStringsToEnum(validatedAllergens);
                        ingredientRepository.persist(newIng);
                        return newIng;
                    } catch (Exception e) {
                        logger.warn("Could not create ingredient '{}': {}", dto.getName(), e.getMessage());
                        return ingredientRepository.findByName(dto.getName()).orElse(null);
                    }
                });

            if (substitutionIngredient == null) {
                continue;
            }

            // Forward: original → substitutionIngredient
            SubstitutionOption option = new SubstitutionOption();
            option.ingredient = original;
            
            SubstitutionAlternative alternative = new SubstitutionAlternative();
            alternative.substitutionOption = option;
            alternative.alternativeIngredient = substitutionIngredient;
            alternative.ratio = 1.0; // Default ratio
            alternative.description = null;
            
            option.alternatives.add(alternative);
            option.persist();
            substitutions.add(option);

            // Reverse: substitutionIngredient → original (only if not already present)
            boolean reverseExists = substitutionIngredient.substitutions.stream()
                .anyMatch(so -> so.alternatives.stream()
                    .anyMatch(a -> a.alternativeIngredient != null && a.alternativeIngredient.id.equals(original.id)));
            if (!reverseExists) {
                SubstitutionOption reverseOption = new SubstitutionOption();
                reverseOption.ingredient = substitutionIngredient;

                SubstitutionAlternative reverseAlt = new SubstitutionAlternative();
                reverseAlt.substitutionOption = reverseOption;
                reverseAlt.alternativeIngredient = original;
                reverseAlt.ratio = 1.0;
                reverseAlt.description = null;

                reverseOption.alternatives.add(reverseAlt);
                reverseOption.persist();
                substitutionIngredient.substitutions.add(reverseOption);
                logger.info("Created reverse substitution: '{}' → '{}'", substitutionIngredient.name, original.name);
            }
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
    public Optional<Ingredient> update(Long id, UpdateIngredientRequestDTO dto) {
        return ingredientRepository.findByIdOptional(id)
            .map(entity -> {
                entity.category = (dto.category == null || dto.category.isBlank())
                    ? null
                    : IngredientCategory.fromDisplayValue(dto.category);
                entity.allergens = dto.allergens == null ? new ArrayList<>()
                    : dto.allergens.stream()
                        .map(a -> {
                            try { return Allergen.valueOf(a); }
                            catch (IllegalArgumentException e) { return null; }
                        })
                        .filter(a -> a != null)
                        .collect(Collectors.toList());
                ingredientRepository.persist(entity);
                return entity;
            });
    }

    @Transactional
    public boolean delete(Long id) {
        return ingredientRepository.deleteById(id);
    }

    @Transactional
    public Optional<IngredientWithSubstitutionsDTO> addSubstitutionOption(Long ingredientId, List<SubstitutionAlternativeInputDTO> alternatives) {
        return ingredientRepository.findByIdOptional(ingredientId).map(ingredient -> {
            SubstitutionOption option = new SubstitutionOption();
            option.ingredient = ingredient;

            for (SubstitutionAlternativeInputDTO altInput : alternatives) {
                Ingredient altIngredient = ingredientRepository.findByIdOptional(altInput.getAlternativeIngredientId()).orElse(null);
                if (altIngredient != null) {
                    SubstitutionAlternative alternative = new SubstitutionAlternative();
                    alternative.substitutionOption = option;
                    alternative.alternativeIngredient = altIngredient;
                    alternative.ratio = altInput.getRatio() != null ? altInput.getRatio() : 1.0;
                    alternative.description = altInput.getDescription();
                    
                    option.alternatives.add(alternative);
                }
            }

            option.persist();
            ingredient.substitutions.add(option);

            // Convert to DTO before returning
            List<SubstitutionOptionResponseDTO> subDTOs = ingredient.substitutions.stream()
                    .map(this::convertSubstitutionOptionToDTO)
                    .collect(Collectors.toList());

            return new IngredientWithSubstitutionsDTO(
                    ingredient.id, ingredient.name, ingredient.unit,
                    ingredient.allergens, subDTOs);
        });
    }

    @Transactional
    public Optional<IngredientWithSubstitutionsDTO> deleteSubstitutionOption(Long ingredientId, Long substitutionId) {
        return ingredientRepository.findByIdOptional(ingredientId).flatMap(ingredient -> {
            SubstitutionOption option = SubstitutionOption.findById(substitutionId);
            if (option != null && option.ingredient.id.equals(ingredientId)) {
                option.delete();
                ingredient.substitutions.remove(option);
                
                List<SubstitutionOptionResponseDTO> subDTOs = ingredient.substitutions.stream()
                        .map(this::convertSubstitutionOptionToDTO)
                        .collect(Collectors.toList());
                
                return Optional.of(new IngredientWithSubstitutionsDTO(
                        ingredient.id, ingredient.name, ingredient.unit,
                        ingredient.allergens, subDTOs));
            }
            return Optional.empty();
        });
    }

    private SubstitutionOptionResponseDTO convertSubstitutionOptionToDTO(SubstitutionOption option) {
        List<SubstitutionAlternativeResponseDTO> alternatives = option.alternatives.stream()
                .map(this::convertSubstitutionAlternativeToDTO)
                .collect(Collectors.toList());

        return new SubstitutionOptionResponseDTO(
                option.id,
                alternatives
        );
    }

    private SubstitutionAlternativeResponseDTO convertSubstitutionAlternativeToDTO(SubstitutionAlternative alternative) {
        Ingredient altIngredient = alternative.alternativeIngredient;
        
        // Don't include nested substitutions to avoid infinite recursion from bidirectional links
        SubstitutionAlternativeResponseDTO dto = new SubstitutionAlternativeResponseDTO(
                altIngredient.id,
                altIngredient.name,
                altIngredient.unit,
                altIngredient.category != null ? altIngredient.category.getDisplayValue() : null,
                altIngredient.allergens,
                alternative.ratio,
                alternative.description,
                List.of()
        );
        return dto;
    }

    public List<SubstitutionDTO> getSubstitutions(String ingredientName) {
        try {
            List<IngredientDTO> substitutionDTOs = substitutionLLMService.findSubstitutions(ingredientName);
            return substitutionDTOs.stream()
                    .map(dto -> new SubstitutionDTO(
                            dto.getName(),
                            dto.getUnit(),
                            dto.getCategory(),
                            dto.getAllergens(),
                            dto.getRatio()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get substitutions for ingredient '{}': {}", ingredientName, e.getMessage());
            return new ArrayList<>();
        }
    }
}
