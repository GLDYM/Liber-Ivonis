package com.liberivonis.integration;

import net.minecraft.network.chat.Component;

public record HubEntry(String id, Component title, Component subtitle, Runnable action, boolean available, String category) {
    public HubEntry(String id, Component title, Component subtitle, Runnable action, boolean available) {
        this(id, title, subtitle, action, available, "");
    }
}
