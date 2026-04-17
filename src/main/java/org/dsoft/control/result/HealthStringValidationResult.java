package org.dsoft.control.result;

import lombok.Data;

@Data
public class HealthStringValidationResult {
    private final String validatedValue;
    private final String errorMessage;

    public static HealthStringValidationResult success(String validatedValue) {
        return new HealthStringValidationResult(validatedValue, null);
    }

    public static HealthStringValidationResult failure(String errorMessage) {
        return new HealthStringValidationResult(null, errorMessage);
    }

    public boolean isSuccess() {
        return errorMessage == null;
    }
}