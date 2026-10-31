package com.liberivonis.config.layout;

/** ARGB theme palette stored in the Hub JSON document. */
public record HubTheme(int background, int surface, int surfaceAlt, int accent, int muted, int outline,
        int disabledOutline) {
    public static HubTheme defaults() {
        return new HubTheme(
                0xFF0D0F15,
                0xFF171A23,
                0xFF2E3547,
                0xFFF2F5F9,
                0xFFAEB8C6,
                0xFF7FD9F5,
                0xFF2B3244);
    }
}
