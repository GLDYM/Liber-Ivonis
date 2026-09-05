package com.liberivonis.client;

import com.liberivonis.client.screen.IvonisHubFragment;
import icyllis.modernui.mc.MuiModApi;

public final class ClientHooks {
    private ClientHooks() {
    }

    public static void openHub() {
        MuiModApi.openScreen(new IvonisHubFragment());
    }
}
