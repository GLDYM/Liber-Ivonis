package com.liberivonis.compat;

import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import net.minecraft.client.Minecraft;

/** Direct compile-time integration for FTB Quests 1.21.1. */
public final class FtbQuestsCompat {
    private FtbQuestsCompat() {
    }

    public static void open() {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null)
            return;
        FTBQuestsClient.openGui();
    }
}
