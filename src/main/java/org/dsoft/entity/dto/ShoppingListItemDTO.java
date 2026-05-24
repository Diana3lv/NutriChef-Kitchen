package org.dsoft.entity.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShoppingListItemDTO {
    public Long id;
    public Long ingredientId;
    public String ingredientName;
    public String category;
    public BigDecimal quantity;
    public String unit;
    public boolean isPurchased;
    public Boolean isChecked;
    public LocalDateTime dateAdded;
    public String notes;
    public BigDecimal ownQuantity;

    public ShoppingListItemDTO() {}

    public ShoppingListItemDTO(
        Long id,
        Long ingredientId,
        String ingredientName,
        String category,
        BigDecimal quantity,
        String unit,
        boolean isPurchased,
        Boolean isChecked,
        LocalDateTime dateAdded,
        String notes,
        BigDecimal ownQuantity
    ) {
        this.id = id;
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.isPurchased = isPurchased;
        this.isChecked = isChecked;
        this.dateAdded = dateAdded;
        this.notes = notes;
        this.ownQuantity = ownQuantity;
    }
}
