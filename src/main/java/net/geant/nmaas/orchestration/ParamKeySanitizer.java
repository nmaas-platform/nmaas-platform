package net.geant.nmaas.orchestration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles conversion of configuration parameter keys between their literal-tag form (as submitted
 * from the deployment wizard, where dots and brackets are encoded as {@code _dot_} and
 * {@code _bracket_} tags) and the Helm-compatible dotted/bracket notation used at deployment time.
 */
public final class ParamKeySanitizer {

    public static final String TAG_DOT = "_dot_";
    public static final String TAG_BRACKET = "_bracket_";

    private ParamKeySanitizer() {
    }

    /**
     * Converts a key from its literal-tag form to the Helm-compatible notation
     * (e.g. {@code targets_bracket_0_bracket__dot_ipAddress} becomes {@code targets[0].ipAddress}).
     */
    public static String sanitizeKey(String name) {
        String processed = name.replace(TAG_DOT, ".");
        if (countBrackets(name) == 2) {
            processed = processed.replaceFirst(TAG_BRACKET, "[").replaceFirst(TAG_BRACKET, "]");
        }
        return processed;
    }

    /**
     * Restores the original, unprocessed form of parameter keys by converting dots and brackets back
     * to their literal tags (e.g. {@code targets[0].Addresses[0].ipAddress} becomes
     * {@code targets_bracket_0_bracket__dot_Addresses_bracket_0_bracket__dot_ipAddress}).
     */
    public static Map<String, String> restoreKeys(Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> restored = new HashMap<>();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            restored.put(restoreKey(entry.getKey()), entry.getValue());
        }
        return restored;
    }

    private static String restoreKey(String name) {
        StringBuilder restored = new StringBuilder();
        int i = 0;
        while (i < name.length()) {
            char c = name.charAt(i);
            if (c == '.') {
                restored.append(TAG_DOT);
                i++;
            } else if (c == '[') {
                // find matching closing bracket
                int close = name.indexOf(']', i);
                if (close < 0) {
                    restored.append(c);
                    i++;
                } else {
                    restored.append(TAG_BRACKET).append(name, i + 1, close).append(TAG_BRACKET);
                    i = close + 1;
                }
            } else {
                restored.append(c);
                i++;
            }
        }
        return restored.toString();
    }

    public static int countBrackets(String text) {
        Matcher m = Pattern.compile(Pattern.quote(TAG_BRACKET)).matcher(text);
        int count = 0;
        while (m.find()) count++;
        return count;
    }

}
