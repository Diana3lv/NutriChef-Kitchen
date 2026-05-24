package org.dsoft.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryIngredientDTO {
    private Long id;
    private IngredientDTO ingredient;
    private String quantity;
    private LocalDate expiryDate;
    private String notes;
    private LocalDate dateAdded;
}
