package org.dsoft.entity.dto;

import org.dsoft.entity.model.UserRole;

public record UserDTO(
    String email,
    String firstName,
    String lastName,
    UserRole role
) {}