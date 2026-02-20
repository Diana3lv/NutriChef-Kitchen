package org.dsoft.dto;

public record AuthResponse(
    String token,
    UserDTO user
) {}