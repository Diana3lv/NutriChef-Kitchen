package org.dsoft.entity.dto;

public record AuthResponse(
    String token,
    UserDTO user
) {}