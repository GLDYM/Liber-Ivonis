package com.liberivonis.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class HubEntriesArgumentInjectionTest {
    @Test
    void injectsFalseForPrimitiveBoolean() {
        Object[] arguments = ScreenArgumentResolver.resolve(
                new Class<?>[] { boolean.class }, new Object[] { null });

        assertArrayEquals(new Object[] { false }, arguments);
    }

    @Test
    void injectsFalseForBoxedBoolean() {
        Object[] arguments = ScreenArgumentResolver.resolve(
                new Class<?>[] { Boolean.class }, new Object[] { null });

        assertArrayEquals(new Object[] { false }, arguments);
    }

    @Test
    void injectsFalseAlongsideClientArguments() {
        Object[] commonArgs = { "client", null };

        Object[] arguments = ScreenArgumentResolver.resolve(
                new Class<?>[] { String.class, boolean.class }, commonArgs);

        assertArrayEquals(new Object[] { "client", false }, arguments);
    }
}
