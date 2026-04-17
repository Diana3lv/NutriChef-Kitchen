package org.dsoft.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDTO {
    private String name;
    private String unit;
    private List<String> allergens;
    private String ratio;  // e.g., "1:1", "3:4", "3 tbsp per 1 cup", etc.
}
