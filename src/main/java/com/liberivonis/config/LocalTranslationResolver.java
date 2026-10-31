package com.liberivonis.config;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/** Resolves local config translations before a global language provider. */
public final class LocalTranslationResolver {
    public static String resolve(String keyOrLiteral, String fallback, Map<String, String> selectedLanguage,
            Map<String, String> fallbackLanguage, Predicate<String> globalExists,
            Function<String, String> globalLookup) {
        if (keyOrLiteral == null || keyOrLiteral.isBlank())
            return fallback == null ? "" : fallback;

        String key = keyOrLiteral.startsWith("key:") ? keyOrLiteral.substring(4) : keyOrLiteral;
        String local = selectedLanguage.get(key);
        if (local != null)
            return local;
        local = fallbackLanguage.get(key);
        if (local != null)
            return local;
        if (globalExists.test(key))
            return globalLookup.apply(key);
        return fallback == null || fallback.isBlank() ? keyOrLiteral : fallback;
    }

    private LocalTranslationResolver() {
    }
}
