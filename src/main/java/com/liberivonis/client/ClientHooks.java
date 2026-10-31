package com.liberivonis.client;

import com.liberivonis.client.screen.IvonisHubFragment;

public final class ClientHooks {
    private ClientHooks() {
    }

    public static void openHub() {
        HubNavigation.openModernScreen(new IvonisHubFragment());
    }
}
