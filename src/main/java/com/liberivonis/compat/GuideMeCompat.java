package com.liberivonis.compat;

import com.liberivonis.client.screen.HandbookEntry;
import guideme.Guide;
import guideme.Guides;
import guideme.internal.GuideMEProxy;
import guideme.internal.MutableGuide;
import guideme.navigation.NavigationNode;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

public final class GuideMeCompat {
    // private static final Logger LOGGER = LoggerFactory.getLogger("LiberIvonis/GuideME");
    private GuideMeCompat() {}
    public static List<HandbookEntry> entries() {
        List<HandbookEntry> result = new ArrayList<>();
        for (Guide guide : Guides.getAll()) {
            var id = guide.getId();
            Component title = null;
            if (guide instanceof MutableGuide mutable) {
                title = mutable.getItemSettings().displayName().orElse(null);
            }
            if (title == null) title = GuideMEProxy.instance().getGuideDisplayName(id);
            if (title == null && guide instanceof MutableGuide mutable) {
                NavigationNode root = mutable.getNavigationTree().getRootNodes().stream().findFirst().orElse(null);
                if (root != null && root.title() != null && !root.title().isBlank()) {
                    title = Component.literal(root.title());
                }
            }
            // LOGGER.info("GuideME entry: id={}, impl={}, itemSettingsTitle={}, proxyTitle={}, finalTitle={}",
            //         id, guide.getClass().getName(),
            //         guide instanceof MutableGuide mutable ? mutable.getItemSettings().displayName().orElse(null) : null,
            //         GuideMEProxy.instance().getGuideDisplayName(id), title);
            if (title == null) title = Component.literal(id.toString());
            Component entryTitle = title;
            result.add(new HandbookEntry(entryTitle, () -> {
                Minecraft client = Minecraft.getInstance();
                if (client.player != null) GuideMEProxy.instance().openGuide(client.player, id);
            }));
        }
        return result;
    }
}
