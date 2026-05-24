package org.dsoft.entity.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dsoft.entity.model.Allergen;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleIngredientDTO {
    private Long id;
    private String name;
    private String unit;
    private List<Allergen> allergens = new ArrayList<>();
}
