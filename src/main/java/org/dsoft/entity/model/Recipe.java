package org.dsoft.entity.model;

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
import lombok.Getter;
import lombok.Setter;
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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @OneToMany(mappedBy = "recipe", fetch = FetchType.EAGER, cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    public List<RecipeIngredient> recipeIngredients = new ArrayList<>();

    @ElementCollection(targetClass = DietaryPreference.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    public List<DietaryPreference> dietaryPreferences = new ArrayList<>();

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

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}