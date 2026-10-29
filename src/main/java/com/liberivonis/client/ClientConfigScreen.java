package com.liberivonis.client;

import com.liberivonis.client.screen.IvonisConfigFragment;
import icyllis.modernui.mc.MuiModApi;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Client-only bridge for the config screen extension point. */
public final class ClientConfigScreen {
    private ClientConfigScreen() {
    }

    public static void register(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, parent) -> create(parent));
    }

    public static Screen create(Screen parent) {
        return MuiModApi.get().createScreen(new IvonisConfigFragment(parent), null, parent);
    }
}
