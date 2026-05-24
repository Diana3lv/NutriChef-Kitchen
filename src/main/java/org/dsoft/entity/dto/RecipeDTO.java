package org.dsoft.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDTO {
    private Long id;
    private String title;
    private String description;
    private String instructions;
    
    @JsonProperty("prepTimeMinutes")
    private Integer prepTimeMinutes;
    
    @JsonProperty("cookTimeMinutes")
    private Integer cookTimeMinutes;
    
    private Integer servings;
    private String difficulty;
    
    @JsonProperty("imageUrl")
    private String imageUrl;
    
    @JsonProperty("sourceUrl")
    private String sourceUrl;
    
    @JsonProperty("sourceApi")
    private String sourceApi;
    
    private Integer calories;
    private Double protein;
    private Double fat;
    private Double carbs;
    private Double fiber;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    private List<RecipeIngredientDTO> ingredients;
    private List<String> tags;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeIngredientDTO {
        private Long ingredientId;
        private String name;
        private String quantity;
        private String unit;
        private List<String> allergens;
    }
}