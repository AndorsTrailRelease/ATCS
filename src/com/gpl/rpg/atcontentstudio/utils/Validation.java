package com.gpl.rpg.atcontentstudio.utils;

import java.util.regex.Pattern;

public final class Validation {
    private static final Pattern INTERNAL_ID_PATTERN = Pattern.compile("[a-z0-9_]+");

    private Validation() {
    }

    public static boolean isValidInternalId(String value) {
        return value != null && !value.isEmpty() && INTERNAL_ID_PATTERN.matcher(value).matches();
    }

    public static String getInternalIdValidationError() {
        return "Internal IDs may only contain lowercase letters, digits, and underscores.";
    }
}
