package com.mycompany.smartcampusapi.util;

import com.mycompany.smartcampusapi.exception.ValidationException;

public final class InputValidator {

    private InputValidator() {
    }

    public static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " is required.");
        }
    }

    public static String requireNonBlank(String value, String fieldName, int maxLength) {
        requireNonNull(value, fieldName);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new ValidationException(fieldName + " must not be blank.");
        }
        if (trimmed.length() > maxLength) {
            throw new ValidationException(fieldName + " must be <= " + maxLength + " characters.");
        }
        return trimmed;
    }

    public static long requirePositiveId(Long value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value <= 0L) {
            throw new ValidationException(fieldName + " must be greater than zero.");
        }
        return value;
    }
}
