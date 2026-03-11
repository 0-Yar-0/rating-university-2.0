package ru.ystu.rating.university;

public final class H2JsonFunctions {

    private H2JsonFunctions() {
    }

    public static String jsonbTypeof(String json) {
        if (json == null) {
            return "null";
        }

        String trimmed = json.trim();
        if (trimmed.startsWith("{")) {
            return "object";
        }
        if (trimmed.startsWith("[")) {
            return "array";
        }
        if ("null".equals(trimmed)) {
            return "null";
        }
        if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
            return "boolean";
        }

        try {
            Double.parseDouble(trimmed);
            return "number";
        } catch (NumberFormatException ignored) {
        }

        return "string";
    }
}
