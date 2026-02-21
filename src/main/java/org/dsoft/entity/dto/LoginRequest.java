package org.dsoft.entity.dto;

public record LoginRequest(
    String email,
    String password
 ) {}