package com.liteflow.dao.reservation;

import java.util.ArrayList;
import java.util.List;

/**
 * Result object for validation operations
 * Contains validation status and error messages
 */
public class ValidationResult {
    private boolean valid;
    private List<String> errors = new ArrayList<>();

    // Constructors
    public ValidationResult() {
        this.valid = true;
    }

    public ValidationResult(boolean valid) {
        this.valid = valid;
    }

    // Getters and Setters
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public void addError(String error) {
        this.errors.add(error);
        this.valid = false; // Automatically set valid to false when adding error
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public String getErrorMessage() {
        return String.join("; ", errors);
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "valid=" + valid +
                ", errors=" + errors +
                '}';
    }
}

