package org.dsoft.entity.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "shopping_list_items",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"user_id", "ingredient_id"},
        name = "uk_user_ingredient_shopping"
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingListItem extends PanacheEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_shopping_list_user"))
    public User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_shopping_list_ingredient"))
    public Ingredient ingredient;

    @Column(nullable = false)
    public BigDecimal quantity;

    @Column(name = "base_quantity")
    public BigDecimal baseQuantity;

    @Column(name = "own_quantity")
    public BigDecimal ownQuantity;

    @Column(name = "unit", nullable = false)
    public String unit;

    @Column(name = "is_purchased", nullable = false)
    public boolean isPurchased = false;

    @Column(name = "is_checked")
    public Boolean isChecked = false;

    @CreationTimestamp
    @Column(name = "date_added", updatable = false)
    public LocalDateTime dateAdded;

    @Column(name = "notes")
    public String notes;
}