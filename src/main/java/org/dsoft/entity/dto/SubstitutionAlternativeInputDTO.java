package org.dsoft.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubstitutionAlternativeInputDTO {
    private Long alternativeIngredientId;
    private Double ratio;
    private String description;
}
