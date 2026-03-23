package org.dsoft.entity.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @ElementCollection(fetch = FetchType.EAGER)
    public List<Ingredient> ingredients = new ArrayList<>();

    @ElementCollection(targetClass = Allergen.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    public List<Allergen> allergens = new ArrayList<>();

    @ElementCollection(targetClass = DietaryPreference.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    public List<DietaryPreference> dietaryPreferences = new ArrayList<>();

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ingredient {
        public String name;
        public String quantity;
        public String unit;
    }
}