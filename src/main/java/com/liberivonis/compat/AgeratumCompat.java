package com.liberivonis.compat;

import dev.anvilcraft.resource.ageratum.client.AgeratumClient;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Direct integration with Ageratum's client guide API. */
public final class AgeratumCompat {
    private AgeratumCompat() {}

    public static boolean open() {
        String language = AgeratumClient.getClientLanguageCode(Minecraft.getInstance()).replace('-', '_');
        return AgeratumClient.openGuideOnClient(
                ResourceLocation.fromNamespaceAndPath("ageratum", "ageratum/" + language + "/guide.md"),
                List.of()
        );
    }
}
