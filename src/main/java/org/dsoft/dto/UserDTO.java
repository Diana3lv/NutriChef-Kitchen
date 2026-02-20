package org.dsoft.dto;

import org.dsoft.entity.UserRole;

public record UserDTO(
    Long id,
    String email,
    String firstName,
    String lastName,
    UserRole role
) {}