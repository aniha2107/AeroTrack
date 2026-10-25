package com.example.baggage.service;

import com.example.baggage.model.Labeled;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Normalisation and validation shared by the admin form and the importers.
 * Blank or "unknown"-style values become null (shown as "Not specified"). Nothing is guessed.
 */
public final class FieldRules {

    private static final Pattern AIRPORT = Pattern.compile("^[A-Z]{3}$");
    private static final Pattern FLIGHT = Pattern.compile("^[A-Z0-9]{2}[0-9]{1,4}[A-Z]?$");
    private static final Pattern ID = Pattern.compile("^[A-Z0-9][A-Z0-9-]{2,39}$");
    private static final Set<String> UNKNOWN = Set.of("not specified", "unknown", "n/a", "na", "-", "none");

    private FieldRules() {
    }

    public static String clean(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public static boolean isUnknown(String s) {
        return s == null || UNKNOWN.contains(s.trim().toLowerCase(Locale.ROOT));
    }

    private static String cleanOrNull(String s) {
        String t = clean(s);
        return (t == null || isUnknown(t)) ? null : t;
    }

    public static String airport(String raw, String field) {
        String v = cleanOrNull(raw);
        if (v == null) {
            return null;
        }
        v = v.toUpperCase(Locale.ROOT);
        if (!AIRPORT.matcher(v).matches()) {
            throw new IllegalArgumentException(field + " must be a 3-letter IATA airport code (got '" + raw.trim() + "')");
        }
        return v;
    }

    public static String flight(String raw, String field) {
        String v = cleanOrNull(raw);
        if (v == null) {
            return null;
        }
        v = v.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        if (!FLIGHT.matcher(v).matches()) {
            throw new IllegalArgumentException(field + " must look like 'AB1234' (got '" + raw.trim() + "')");
        }
        return v;
    }

    public static String id(String raw, String field) {
        String v = cleanOrNull(raw);
        if (v == null) {
            return null;
        }
        v = v.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        if (!ID.matcher(v).matches()) {
            throw new IllegalArgumentException(field + " must be 3-40 letters, digits or hyphens (got '" + raw.trim() + "')");
        }
        return v;
    }

    public static String text(String raw, int max, String field) {
        String v = cleanOrNull(raw);
        if (v == null) {
            return null;
        }
        if (v.length() > max) {
            throw new IllegalArgumentException(field + " is longer than " + max + " characters");
        }
        return v;
    }

    /** Parses an enum by constant name or label. Blank/unknown -> null; anything else unrecognised is an error. */
    public static <E extends Enum<E> & Labeled> E enumValue(String raw, E[] values, String field) {
        String v = cleanOrNull(raw);
        if (v == null) {
            return null;
        }
        String norm = normalise(v);
        for (E e : values) {
            if (normalise(e.name()).equals(norm) || normalise(e.getLabel()).equals(norm)) {
                return e;
            }
        }
        throw new IllegalArgumentException(field + " has an unrecognised value '" + v + "'");
    }

    private static String normalise(String s) {
        return s.toLowerCase(Locale.ROOT).replace('_', ' ').replace('-', ' ').replaceAll("\\s+", " ").trim();
    }
}
