package org.dsoft.entity.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ingredient_substitutions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IngredientSubstitution extends PanacheEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "original_ingredient_id", nullable = false)
    public Ingredient originalIngredient;

    @Column(nullable = false)
    public Double originalQuantity;

    @Column(nullable = false)
    public String originalUnit;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "substitution_ingredients",
        joinColumns = @JoinColumn(name = "substitution_id"),
        inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    public List<Ingredient> ingredients = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "substitution_ratios", joinColumns = @JoinColumn(name = "substitution_id"))
    @Column(name = "ratio")
    public List<String> ratios = new ArrayList<>();
}