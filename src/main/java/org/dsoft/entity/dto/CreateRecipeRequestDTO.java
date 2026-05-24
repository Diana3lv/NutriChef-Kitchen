package org.dsoft.entity.dto;

import java.util.List;

public class CreateRecipeRequestDTO {
    public String title;
    public String description;
    public String instructions;
    public Integer prepTimeMinutes;
    public Integer cookTimeMinutes;
    public Integer servings;
    public String difficulty;
    public String imageUrl;
    public List<String> tags;
    public List<IngredientItemDTO> ingredients;

    public static class IngredientItemDTO {
        public Long ingredientId;
        public String quantity;
    }
}
