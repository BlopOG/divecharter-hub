package com.divecharter.hub.utils;

import java.util.regex.Pattern;

/** Cleans free-text input before it is stored. */
public final class InputSanitizer {

    private static final Pattern HTML_TAGS = Pattern.compile("<[^>]*>");

    private InputSanitizer() {
    }

    /** Removes HTML tags and surrounding spaces. Blank input becomes null. */
    public static String clean(String input) {
        if (input == null) {
            return null;
        }
        String cleaned = HTML_TAGS.matcher(input).replaceAll("").trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}