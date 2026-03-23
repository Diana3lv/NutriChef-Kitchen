package org.dsoft.control;

import org.dsoft.entity.dto.NutritionProfileDTO;
import org.dsoft.entity.model.Allergen;
import org.dsoft.entity.model.DietaryPreference;
import org.dsoft.entity.model.NutritionProfile;
import org.dsoft.entity.model.User;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class NutritionProfileService {

    @Inject
    HealthConditionParser healthConditionParser;

    public Optional<NutritionProfile> findByUser(User user) {
        return NutritionProfile.find("user", user).firstResultOptional();
    }

    @Transactional
    public NutritionProfileDTO getNutritionProfileByUserId(Long userId) {
        User user = User.findById(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        NutritionProfile nutritionProfile = findByUser(user).orElse(new NutritionProfile());
        
        NutritionProfileDTO dto = new NutritionProfileDTO();
        dto.setAllergens(enumListToNames(nutritionProfile.allergens));
        dto.setDietaryPreferences(enumListToNames(nutritionProfile.dietaryPreferences));
        dto.setMedicalConditions(nutritionProfile.medicalConditions);
        dto.setParsedAvoidIngredients(nutritionProfile.parsedAvoidIngredients);
        
        return dto;
    }

    @Transactional
    public void updateNutritionProfile(Long userId, NutritionProfileDTO nutritionProfileDTO) {
        User user = User.findById(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        NutritionProfile nutritionProfile = findByUser(user).orElseGet(() -> {
            NutritionProfile newProfile = new NutritionProfile();
            newProfile.user = user;
            return newProfile;
        });

        nutritionProfile.allergens = parseAllergens(nutritionProfileDTO.getAllergens());
        nutritionProfile.dietaryPreferences = parseDietaryPreferences(nutritionProfileDTO.getDietaryPreferences());
        nutritionProfile.medicalConditions = nutritionProfileDTO.getMedicalConditions();

        if (nutritionProfileDTO.getMedicalConditions() != null && !nutritionProfileDTO.getMedicalConditions().isBlank()) {
            var parsedIngredients = healthConditionParser.parseHealthConditions(
                nutritionProfileDTO.getMedicalConditions(),
                nutritionProfileDTO.getIntolerances()
            );
            nutritionProfile.parsedAvoidIngredients = parsedIngredients;
        }

        nutritionProfile.persist();
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