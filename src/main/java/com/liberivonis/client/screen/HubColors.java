package com.liberivonis.client.screen;

import com.liberivonis.config.layout.HubLayoutLoader;
import com.liberivonis.config.layout.HubTheme;

/** Resolved client-side colour palette for the Hub UI. */
record HubColors(int background, int surface, int surfaceAlt, int accent, int muted, int outline,
        int disabledOutline) {
    static HubColors current() {
        return from(HubLayoutLoader.load().theme());
    }

    static HubColors from(HubTheme theme) {
        return new HubColors(
                theme.background(),
                theme.surface(),
                theme.surfaceAlt(),
                theme.accent(),
                theme.muted(),
                theme.outline(),
                theme.disabledOutline());
    }
}
