package org.dsoft.entity.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dsoft.entity.dto.IngredientDTO;
import org.dsoft.entity.dto.InventoryIngredientDTO;

@Entity
@Table(name = "inventory_ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryIngredient extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    public Inventory inventory;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ingredient_id", nullable = false)
    public Ingredient ingredient;

    @Column(nullable = false)
    public String quantity;

    @Column(nullable = true)
    public LocalDate expiryDate;

    @Column(nullable = true, length = 256)
    public String notes;

    @Column(nullable = false)
    public LocalDate dateAdded;

    @PrePersist
    protected void onCreate() {
        if (dateAdded == null) {
            dateAdded = LocalDate.now();
        }
    }

    public InventoryIngredientDTO toDTO() {
        IngredientDTO ingDTO = new IngredientDTO(
                this.ingredient.name,
                this.ingredient.unit,
                this.ingredient.allergens.stream()
                        .map(Enum::name)
                        .collect(Collectors.toList()),
                null  // ratio field is for substitutions only, not used in inventory
        );

        return new InventoryIngredientDTO(
                this.id,
                ingDTO,
                this.quantity,
                this.expiryDate,
                this.notes,
                this.dateAdded
        );
    }
}
