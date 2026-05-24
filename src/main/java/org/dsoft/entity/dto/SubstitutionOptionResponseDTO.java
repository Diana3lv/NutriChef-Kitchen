package org.dsoft.entity.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubstitutionOptionResponseDTO {
    private Long id;
    private List<SubstitutionAlternativeResponseDTO> alternatives = new ArrayList<>();
}
