package org.dsoft.dto;

public record LoginRequest(
    String email,
    String password
 ) {}