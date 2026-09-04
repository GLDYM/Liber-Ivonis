package com.liberivonis.client;

import com.liberivonis.LiberIvonis;
import com.liberivonis.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.neoforged.fml.ModList;

@EventBusSubscriber(modid = LiberIvonis.MOD_ID, value = Dist.CLIENT)
public final class ClientEventHandlers {
    public static final KeyMapping OPEN_HUB = new KeyMapping("key.liber_ivonis.open_hub", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.liber_ivonis");
    private ClientEventHandlers() {}

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) { event.register(OPEN_HUB); }

    @SubscribeEvent
    public static void clientSetup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
        if (ModList.get().isLoaded("ftblibrary")) event.enqueueWork(com.liberivonis.compat.FtbLibraryCompat::init);
    }

    @SubscribeEvent
    public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (OPEN_HUB.consumeClick() && mc.player != null && mc.screen == null) ClientHooks.openHub();
    }

    @SubscribeEvent
    public static void onMouseClick(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.getKeyMapping() != Minecraft.getInstance().options.keyUse || Minecraft.getInstance().player == null) return;
        if (Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.IVONIS_BOOK.get())) {
            event.setCanceled(true);
            ClientHooks.openHub();
        }
    }
}
