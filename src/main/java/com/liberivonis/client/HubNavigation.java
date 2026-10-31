package com.liberivonis.client;

import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.mc.MuiModApi;
import icyllis.modernui.mc.MuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Handles screen transitions while preserving the previous screen for Back/Esc. */
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

    /** Opens a ModernUI fragment as a screen whose Back/Esc target is the current screen. */
    public static void openModernScreen(Fragment fragment) {
        Minecraft client = Minecraft.getInstance();
        Screen previous = client.screen;
        client.execute(() -> {
            Screen screen = MuiModApi.get().createScreen(fragment, null, previous);
            client.setScreen(screen);
        });
    }

    /** Closes the current ModernUI screen and returns to the screen that opened it. */
    public static void closeModernScreen() {
        Minecraft client = Minecraft.getInstance();
        Screen current = client.screen;
        Screen previous = current instanceof MuiScreen muiScreen ? muiScreen.getPreviousScreen() : null;
        client.execute(() -> {
            if (client.screen == current)
                client.setScreen(previous);
        });
    }
}
