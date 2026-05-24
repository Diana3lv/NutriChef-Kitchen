package org.dsoft.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthConditionDTO {
    
    private String healthConditions;
    private String intolerances;
    
    private List<String> parsedAvoidIngredients;
}