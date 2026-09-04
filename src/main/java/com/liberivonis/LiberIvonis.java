package com.liberivonis;

import com.liberivonis.config.IvonisConfig;
import com.liberivonis.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraft.world.item.CreativeModeTabs;

@Mod(LiberIvonis.MOD_ID)
public final class LiberIvonis {
    public static final String MOD_ID = "liber_ivonis";

    public LiberIvonis(IEventBus modEventBus, ModContainer container) {
        ModItems.ITEMS.register(modEventBus);
        container.registerConfig(ModConfig.Type.CLIENT, IvonisConfig.SPEC);
        modEventBus.addListener(this::addCreativeTabContents);
    }

    private void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES)
            event.accept(ModItems.IVONIS_BOOK);
    }
}
