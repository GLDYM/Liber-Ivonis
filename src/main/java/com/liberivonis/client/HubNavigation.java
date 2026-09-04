package com.liberivonis.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Handles the safe transition from ModernUI Hub to a native child screen. */
public final class HubNavigation {
    private static boolean switchingToChild;

    private HubNavigation() {
    }

    public static void openChild(Screen child) {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            switchingToChild = true;
            client.setScreen(null);
            // Let ModernUI finish removing its SimpleScreen before installing
            // the vanilla/third-party screen. This avoids UIManager races.
            client.execute(() -> {
                client.setScreen(child);
                switchingToChild = false;
            });
        });
    }

    public static void openAction(Runnable action) {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            client.setScreen(null);
            client.execute(action);
        });
    }
}
