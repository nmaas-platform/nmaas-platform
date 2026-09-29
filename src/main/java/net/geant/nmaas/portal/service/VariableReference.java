package net.geant.nmaas.portal.service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for parsing and resolving references to centrally stored variables
 * inside application deployment parameter values.
 * <p>
 * A variable reference uses the notation {@code ${var:NAME}} where {@code NAME}
 * is the name of a variable kept in the central variable storage. Any other
 * string is treated as a plain static value.
 */
public final class VariableReference {

    /**
     * Notation prefix distinguishing a variable reference from a plain static string.
     */
    public static final String REFERENCE_PREFIX = "${var:";

    public static final String REFERENCE_SUFFIX = "}";

    private static final Pattern REFERENCE_PATTERN = Pattern.compile("\\$\\{var:([A-Za-z0-9_.-]+)}");

    private VariableReference() {
    }

    /**
     * Checks whether the given parameter value is a single variable reference
     * ({@code ${var:NAME}} with a non-empty, well-formed name).
     */
    public static boolean isReference(String value) {
        return extractName(value).isPresent();
    }

    /**
     * Extracts the referenced variable name from a value that {@link #isReference(String)} matched.
     */
    public static Optional<String> extractName(String value) {
        if (value == null) {
            return Optional.empty();
        }
        Matcher matcher = REFERENCE_PATTERN.matcher(value.trim());
        if (matcher.matches()) {
            return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }

}
