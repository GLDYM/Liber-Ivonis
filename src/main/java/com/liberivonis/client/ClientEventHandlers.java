package com.liberivonis.client;

import com.liberivonis.LiberIvonis;
import com.liberivonis.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = LiberIvonis.MOD_ID, value = Dist.CLIENT)
public final class ClientEventHandlers {
    private ClientEventHandlers() {}

    @SubscribeEvent
    public static void onMouseClick(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.getKeyMapping() != Minecraft.getInstance().options.keyUse || Minecraft.getInstance().player == null) return;
        if (Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.IVONIS_BOOK.get())) {
            event.setCanceled(true);
            ClientHooks.openHub();
        }
    }
}
