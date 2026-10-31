package com.liberivonis.config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalTranslationResolverTest {
    private static final Map<String, String> GLOBAL = Map.of(
            "hub.title", "Global title",
            "only.global", "Global only");

    @Test
    void selectedLocalTranslationWins() {
        assertEquals("Local title", resolve("hub.title", "Fallback",
                Map.of("hub.title", "Local title"), Map.of("hub.title", "English local")));
    }

    @Test
    void languageFallbackWinsOverGlobal() {
        assertEquals("English local", resolve("hub.title", "Fallback",
                Map.of(), Map.of("hub.title", "English local")));
    }

    @Test
    void globalTranslationIsUsedWhenLocalEntriesAreMissing() {
        assertEquals("Global only", resolve("only.global", "Fallback", Map.of(), Map.of()));
    }

    @Test
    void fallbackAndLiteralAreUsedLast() {
        assertEquals("Fallback", resolve("missing.key", "Fallback", Map.of(), Map.of()));
        assertEquals("Plain text", resolve("Plain text", "", Map.of(), Map.of()));
    }

    @Test
    void legacyKeyPrefixIsAccepted() {
        assertEquals("Local title", resolve("key:hub.title", "Fallback",
                Map.of("hub.title", "Local title"), Map.of()));
    }

    private static String resolve(String value, String fallback, Map<String, String> selected,
            Map<String, String> english) {
        return LocalTranslationResolver.resolve(value, fallback, selected, english,
                GLOBAL::containsKey, GLOBAL::get);
    }
}
