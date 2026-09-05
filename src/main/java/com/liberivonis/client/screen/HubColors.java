package com.liberivonis.client.screen;

import com.liberivonis.config.IvonisConfig;

/** Resolved client-side colour palette for the Hub UI. */
record HubColors(int background, int surface, int surfaceAlt, int accent, int muted, int outline,
        int disabledOutline) {
    static HubColors current() {
        return new HubColors(
                parse(IvonisConfig.hubBackground.get()),
                parse(IvonisConfig.hubSurface.get()),
                parse(IvonisConfig.hubSurfaceAlt.get()),
                parse(IvonisConfig.hubAccent.get()),
                parse(IvonisConfig.hubMuted.get()),
                parse(IvonisConfig.hubOutline.get()),
                parse(IvonisConfig.hubDisabledOutline.get()));
    }

    private static int parse(String value) {
        String hex = value.substring(1);
        if (hex.length() == 6)
            hex = "FF" + hex;
        return (int) Long.parseLong(hex, 16);
    }
}
