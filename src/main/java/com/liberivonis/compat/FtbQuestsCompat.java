package com.liberivonis.compat;

import dev.ftb.mods.ftbquests.client.FTBQuestsClient;

/** Direct compile-time integration for FTB Quests 1.21.1. */
public final class FtbQuestsCompat {
    private FtbQuestsCompat() {}

    public static void open() {
        FTBQuestsClient.openGui();
    }
}
