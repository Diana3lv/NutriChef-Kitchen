package org.dsoft.entity.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
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

    @Column(nullable = false, unique = true)
    public String primaryIngredient;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "substitution_alternatives", joinColumns = @JoinColumn(name = "ingredient_substitution_id"))
    public List<Substitution> alternatives;

    @Enumerated(EnumType.STRING)
    public DietaryPreference applicableTo;
}
