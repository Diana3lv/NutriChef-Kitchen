package org.dsoft.entity.dto;

import java.util.List;

public record SubstitutionDTO(
        String name,
        String unit,
        String category,
        List<String> allergens,
        String ratio
) {}