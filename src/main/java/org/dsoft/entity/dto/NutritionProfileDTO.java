package org.dsoft.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class NutritionProfileDTO {

    private List<String> dietaryPreferences;
    private List<String> allergens;
    private String medicalConditions;
}