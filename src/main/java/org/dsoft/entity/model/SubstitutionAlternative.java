package org.dsoft.entity.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "substitution_alternatives")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubstitutionAlternative extends PanacheEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "substitution_option_id", nullable = false)
    public SubstitutionOption substitutionOption;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "alternative_ingredient_id", nullable = false)
    public Ingredient alternativeIngredient;

    /**
     * The ratio/quantity needed for this substitution.
     * For example: 1 egg = 2 * applesauce (ratio = 2.0)
     */
    public Double ratio;

    /**
     * Optional description for complex substitutions
     * For example: "2 * applesauce + 3 * water"
     */
    public String description;
}
