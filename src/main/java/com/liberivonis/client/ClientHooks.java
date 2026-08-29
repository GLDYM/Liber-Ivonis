package com.liberivonis.client;

import com.liberivonis.client.screen.IvonisHubFragment;
import icyllis.modernui.mc.MuiModApi;
import net.minecraft.client.Minecraft;

public final class ClientHooks {
    private ClientHooks() {}
    public static void openHub() { MuiModApi.openScreen(new IvonisHubFragment()); }
}
