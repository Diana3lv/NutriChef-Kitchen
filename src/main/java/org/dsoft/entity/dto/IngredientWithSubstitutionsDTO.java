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
public class IngredientWithSubstitutionsDTO {
    private Long id;
    private String name;
    private String unit;
    private String category;
    private List<Allergen> allergens = new ArrayList<>();
    private List<SubstitutionOptionResponseDTO> substitutions = new ArrayList<>();

    public IngredientWithSubstitutionsDTO(Long id, String name, String unit,
                                          List<Allergen> allergens, List<SubstitutionOptionResponseDTO> substitutions) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.allergens = allergens;
        this.substitutions = substitutions;
    }
}
