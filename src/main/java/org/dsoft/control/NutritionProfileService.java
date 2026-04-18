package org.dsoft.control;

import org.dsoft.control.parser.AllergenParser;
import org.dsoft.control.parser.DietaryPreferencesParser;
import org.dsoft.control.parser.HealthConditionParser;
import org.dsoft.entity.dto.NutritionProfileDTO;
import org.dsoft.entity.model.Allergen;
import org.dsoft.entity.model.DietaryPreference;
import org.dsoft.entity.model.NutritionProfile;
import org.dsoft.entity.model.User;
import org.dsoft.repository.NutritionProfileRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class NutritionProfileService {

    @Inject
    HealthConditionParser healthConditionParser;

    @Inject
    AllergenParser allergenParser;

    @Inject
    DietaryPreferencesParser dietaryPreferencesParser;
    
    @Inject
    NutritionProfileRepository nutritionProfileRepository;
    
    @Inject
    UserService userService;

    public Optional<NutritionProfile> findByUser(User user) {
        return nutritionProfileRepository.findByUser(user);
    }

    @Transactional
    public NutritionProfileDTO getNutritionProfileByUserId(Long userId) {
        User user = userService.getUserById(userId);

        NutritionProfile nutritionProfile = findByUser(user).orElse(new NutritionProfile());
        
        NutritionProfileDTO dto = new NutritionProfileDTO();
        dto.setAllergens(enumListToNames(nutritionProfile.allergens));
        dto.setDietaryPreferences(enumListToNames(nutritionProfile.dietaryPreferences));
        dto.setMedicalConditions(nutritionProfile.medicalConditions);
        dto.setIntolerances(nutritionProfile.intolerances);
        dto.setParsedAvoidIngredients(nutritionProfile.parsedAvoidIngredients);
        return dto;
    }

    @Transactional
    public NutritionProfileDTO updateNutritionProfile(Long userId, NutritionProfileDTO nutritionProfileDTO) {
        User user = userService.getUserById(userId);

        NutritionProfile nutritionProfile = findByUser(user).orElseGet(() -> {
            NutritionProfile newProfile = new NutritionProfile();
            newProfile.user = user;
            return newProfile;
        });

        nutritionProfile.allergens = parseAllergens(nutritionProfileDTO.getAllergens());
        nutritionProfile.dietaryPreferences = parseDietaryPreferences(nutritionProfileDTO.getDietaryPreferences());

        // Validate and update medical conditions
        String providedMedicalConditions = nutritionProfileDTO.getMedicalConditions();
        if (providedMedicalConditions != null && !providedMedicalConditions.isBlank()) {
            nutritionProfile.medicalConditions = healthConditionParser.cleanAndValidateHealthString(providedMedicalConditions).orElse(null);
        }

        // Validate and update intolerances
        String providedIntolerances = nutritionProfileDTO.getIntolerances();
        if (providedIntolerances != null && !providedIntolerances.isBlank()) {
            nutritionProfile.intolerances = healthConditionParser.cleanAndValidateHealthString(providedIntolerances).orElse(null);
        }

        nutritionProfile.parsedAvoidIngredients = buildAvoidedIngredientsSet(
            nutritionProfile.allergens,
            nutritionProfile.dietaryPreferences,
            nutritionProfile.medicalConditions,
            nutritionProfile.intolerances);

        nutritionProfileRepository.persist(nutritionProfile);

        NutritionProfileDTO responseDTO = new NutritionProfileDTO();
        responseDTO.setAllergens(enumListToNames(nutritionProfile.allergens));
        responseDTO.setDietaryPreferences(enumListToNames(nutritionProfile.dietaryPreferences));
        responseDTO.setMedicalConditions(nutritionProfile.medicalConditions);
        responseDTO.setIntolerances(nutritionProfile.intolerances);
        responseDTO.setParsedAvoidIngredients(nutritionProfile.parsedAvoidIngredients);
        return responseDTO;
    }

    private Set<String> buildAvoidedIngredientsSet(List<Allergen> allergens,
                                                     List<DietaryPreference> dietaryPreferences,
                                                     String medicalConditions,
                                                     String intolerances) {
        Set<String> avoidIngredients = new java.util.HashSet<>();
        avoidIngredients.addAll(allergenParser.getAllAllergenIngredients(allergens));
        avoidIngredients.addAll(dietaryPreferencesParser.getAllIncompatibleFoods(dietaryPreferences));
        addHealthConditionIngredientsIfPresent(avoidIngredients, medicalConditions, intolerances);
        return avoidIngredients;
    }

    private void addHealthConditionIngredientsIfPresent(Set<String> avoidIngredients,
                                                        String medicalConditions,
                                                        String intolerances) {
        if (fieldIsNotEmpty(medicalConditions)) {
            Set<String> medicalIngredients = healthConditionParser.parseHealthConditions(medicalConditions, null);
            if (medicalIngredients != null) {
                avoidIngredients.addAll(medicalIngredients);
            }
        }
        if (fieldIsNotEmpty(intolerances)) {
            Set<String> toleranceIngredients = healthConditionParser.parseHealthConditions(null, intolerances);
            if (toleranceIngredients != null) {
                avoidIngredients.addAll(toleranceIngredients);
            }
        }
    }

    private boolean fieldIsNotEmpty(String field) {
        return field != null && !field.isBlank();
    }

    private List<String> enumListToNames(List<? extends Enum<?>> values) {
        if (values == null) {
            return Collections.emptyList();
        }

        return values.stream().map(Enum::name).toList();
    }

    @SuppressWarnings("null")
    private List<Allergen> parseAllergens(List<String> values) {
        if (values == null) {
            return Collections.emptyList();
        }

        try {
            return values.stream()
                .map(this::normalizeEnumValue)
                .map(Allergen::valueOf)
                .collect(Collectors.toList());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid allergen. Allowed values: " +
                Arrays.stream(Allergen.values()).map(Enum::name).collect(Collectors.joining(", ")));
        }
    }

    @SuppressWarnings("null")
    private List<DietaryPreference> parseDietaryPreferences(List<String> values) {
        if (values == null) {
            return Collections.emptyList();
        }

        try {
            return values.stream()
                .map(this::normalizeEnumValue)
                .map(DietaryPreference::valueOf)
                .collect(Collectors.toList());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid dietary preference. Allowed values: " +
                Arrays.stream(DietaryPreference.values()).map(Enum::name).collect(Collectors.joining(", ")));
        }
    }

    private String normalizeEnumValue(String raw) {
        if (raw == null) {
            throw new BadRequestException("Enum values cannot be null");
        }

        return raw.trim().replace('-', '_').replace(' ', '_').toUpperCase();
    }

}