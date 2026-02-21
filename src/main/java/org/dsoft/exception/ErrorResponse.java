package org.dsoft.exception;

public record ErrorResponse(
    String error,
    int status
) {}
