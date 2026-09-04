package com.liberivonis.client.screen;

import net.minecraft.network.chat.Component;

public record HandbookEntry(Component title, Runnable action) {
}
