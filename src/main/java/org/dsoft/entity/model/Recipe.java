package org.dsoft.entity.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.dsoft.entity.dto.RecipeDTO;
import org.dsoft.entity.dto.RecipeDTO.RecipeIngredientDTO;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "recipes")
@Getter
@Setter
public class Recipe extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String title;

    @Column(length = 1000)
    public String description;

    @Column(columnDefinition = "TEXT")
    public String instructions;

    @Column(name = "prep_time_minutes")
    public Integer prepTimeMinutes;

    @Column(name = "cook_time_minutes")
    public Integer cookTimeMinutes;

    public Integer servings;

    @Enumerated(EnumType.STRING)
    public Difficulty difficulty;

    @Column(name = "image_url")
    public String imageUrl;

    @Column(name = "source_url")
    public String sourceUrl;

    @Column(name = "source_api")
    public String sourceApi;

    @Column(name = "calories")
    public Integer calories;

    @Column(name = "protein_grams")
    public Double proteinGrams;

    @Column(name = "fat_grams")
    public Double fatGrams;

    @Column(name = "carbs_grams")
    public Double carbsGrams;

    @Column(name = "fiber_grams")
    public Double fiberGrams;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @OneToMany(mappedBy = "recipe", fetch = FetchType.EAGER, cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("recipe-ingredients")
    public List<RecipeIngredient> recipeIngredients = new ArrayList<>();

    @ElementCollection(targetClass = DietaryPreference.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    public List<DietaryPreference> dietaryPreferences = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    public Set<RecipeTag> tags = new HashSet<>();

    public List<Allergen> getAllergens() {
        if (recipeIngredients == null || recipeIngredients.isEmpty()) {
            return new ArrayList<>();
        }
        
        Set<Allergen> allergenSet = new HashSet<>();
        for (RecipeIngredient recipeIng : recipeIngredients) {
            if (recipeIng.ingredient != null && recipeIng.ingredient.allergens != null) {
                allergenSet.addAll(recipeIng.ingredient.allergens);
            }
        }
        
        return new ArrayList<>(allergenSet);
    }

    public RecipeDTO toRecipeDTO() {
        RecipeDTO dto = new RecipeDTO();
        dto.setId(this.id);
        dto.setTitle(this.title);
        dto.setDescription(this.description);
        dto.setInstructions(this.instructions);
        dto.setPrepTimeMinutes(this.prepTimeMinutes);
        dto.setCookTimeMinutes(this.cookTimeMinutes);
        dto.setServings(this.servings);
        dto.setDifficulty(this.difficulty != null ? this.difficulty.name() : null);
        dto.setImageUrl(this.imageUrl);
        dto.setSourceUrl(this.sourceUrl);
        dto.setSourceApi(this.sourceApi);
        dto.setCalories(this.calories);
        dto.setProtein(this.proteinGrams);
        dto.setFat(this.fatGrams);
        dto.setCarbs(this.carbsGrams);
        dto.setFiber(this.fiberGrams);
        dto.setCreatedAt(this.createdAt);
        dto.setUpdatedAt(this.updatedAt);
        dto.setTags(this.tags != null
            ? this.tags.stream().map(Enum::name).collect(Collectors.toList())
            : List.of());
        
        if (this.recipeIngredients != null && !this.recipeIngredients.isEmpty()) {
            List<RecipeIngredientDTO> flatIngredients = this.recipeIngredients.stream()
                    .map(ri -> new RecipeIngredientDTO(
                            ri.ingredient.id,
                            ri.ingredient.name,
                            ri.quantity,
                            ri.ingredient.unit,
                            ri.ingredient.allergens.stream()
                                    .map(Enum::name)
                                    .collect(Collectors.toList())
                    ))
                    .collect(Collectors.toList());
            dto.setIngredients(flatIngredients);
        }
        
        return dto;
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}