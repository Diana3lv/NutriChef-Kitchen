package org.dsoft.entity.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddSubstitutionRequestDTO {
    private List<SubstitutionAlternativeInputDTO> alternatives;
}
