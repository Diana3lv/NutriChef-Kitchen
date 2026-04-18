package org.dsoft.entity.dto;

import java.util.List;

public record SubstitutionDTO(
        String name,
        String unit,
        List<String> allergens,
        String ratio
) {}