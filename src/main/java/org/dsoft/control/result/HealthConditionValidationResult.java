package org.dsoft.control.result;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Validation result for health conditions and intolerances.
 * Distinguishes between:
 * - SUCCESS: LLM validated and approved the input
 * - REJECTED: LLM explicitly rejected the input as invalid
 * - API_ERROR: LLM was unavailable (API down, network error, etc.)
 */
@Data
@AllArgsConstructor
public class HealthConditionValidationResult {
    private final Status status;
    private final String value;  // The validated/input value
    private final String errorMessage;  // Error message if rejected or API error

    public enum Status {
        SUCCESS,       // LLM validated successfully
        REJECTED,      // LLM explicitly rejected as invalid
        API_ERROR      // LLM unavailable or network error
    }

    public static HealthConditionValidationResult success(String value) {
        return new HealthConditionValidationResult(Status.SUCCESS, value, null);
    }

    public static HealthConditionValidationResult rejected(String value) {
        return new HealthConditionValidationResult(
            Status.REJECTED,
            null,
            "'" + value + "' is not a valid medical condition or food intolerance. Please enter a real condition or allergy (e.g., 'diabetes', 'lactose intolerance') or leave it blank."
        );
    }

    public static HealthConditionValidationResult apiError(String value) {
        return new HealthConditionValidationResult(
            Status.API_ERROR,
            value,  // Still return the value so we can save it
            null
        );
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isRejected() {
        return status == Status.REJECTED;
    }

    public boolean isApiError() {
        return status == Status.API_ERROR;
    }
}
