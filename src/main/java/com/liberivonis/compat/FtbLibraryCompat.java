package com.liberivonis.compat;

import com.liberivonis.client.ClientHooks;
import dev.ftb.mods.ftblibrary.api.sidebar.SidebarButtonCreatedEvent;
import dev.ftb.mods.ftblibrary.ui.CustomClickEvent;
import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;

/** FTBLibrary sidebar integration. The actual button is declared as a sidebar resource. */
public final class FtbLibraryCompat {
    public static final ResourceLocation BUTTON_ID = ResourceLocation.fromNamespaceAndPath("liber_ivonis", "ivonis_book");
    public static void init() {
        CustomClickEvent.EVENT.register(event -> event.id().equals(ResourceLocation.fromNamespaceAndPath("liber_ivonis", "open_hub"))
                ? openHub() : EventResult.pass());
        SidebarButtonCreatedEvent.EVENT.register(event -> {
            if (event.getButton().getId().equals(BUTTON_ID)) {
                event.getButton().setTooltipOverride(() -> java.util.List.of(net.minecraft.network.chat.Component.translatable("item.liber_ivonis.ivonis_book")));
            }
        });
    }
    private static EventResult openHub() {
        ClientHooks.openHub();
        return EventResult.interruptTrue();
    }
    private FtbLibraryCompat() {}
}
