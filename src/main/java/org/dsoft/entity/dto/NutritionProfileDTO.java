package org.dsoft.entity.dto;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class NutritionProfileDTO {

    private List<String> dietaryPreferences;
    private List<String> allergens;
    private String medicalConditions;
    private String intolerances;
    
    private Set<String> parsedAvoidIngredients;
}