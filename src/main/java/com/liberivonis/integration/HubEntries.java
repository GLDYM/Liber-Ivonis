package com.liberivonis.integration;

import com.liberivonis.config.IvonisConfig;
import com.liberivonis.compat.FtbQuestsCompat;
import com.liberivonis.compat.PatchouliCompat;
import com.liberivonis.compat.GuideMeCompat;
import com.liberivonis.compat.AgeratumCompat;
import com.liberivonis.client.screen.HandbookCategoryFragment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import com.liberivonis.client.screen.HandbookEntry;
import com.liberivonis.client.screen.HandbookCategoryFragment;
import icyllis.modernui.mc.MuiModApi;

public final class HubEntries {
    public static List<HubEntry> collect() {
        List<HubEntry> result = new ArrayList<>();
        Map<String, HubEntry> builtins = builtins();
        for (String value : IvonisConfig.builtinEntries.get()) {
            String[] spec = value.split("\\|", 2);
            HubEntry entry = builtins.get(spec[0].trim());
            if (entry != null && (entry.available() || IvonisConfig.showUnavailable.get())) {
                String category = spec.length > 1 ? spec[1].trim() : "";
                result.add(category.isEmpty() ? entry : new HubEntry(entry.id(), entry.title(), entry.subtitle(), entry.action(), entry.available(), category));
            }
        }
        addConfiguredScreens(result);
        for (String value : IvonisConfig.handbookItems.get()) {
            String[] p = value.split("\\|", 3);
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(p[0]));
            if (item != BuiltInRegistries.ITEM.get(BuiltInRegistries.ITEM.getDefaultKey())) {
                Component title = p.length > 1 ? configurableTitle(p[1]) : item.getName(item.getDefaultInstance());
                String category = p.length > 2 && !p[2].isBlank() ? p[2].trim() : "";
                result.add(new HubEntry("item:" + p[0], title, Component.translatable("entry.liber_ivonis.configured_handbook.description"), () -> useHandbook(item), true, category));
            }
        }
        // Keep uncategorized entries at the top, then render each category as a group.
        Map<String, List<HubEntry>> groups = new LinkedHashMap<>();
        for (HubEntry entry : result) groups.computeIfAbsent(entry.category(), ignored -> new ArrayList<>()).add(entry);
        List<HubEntry> ordered = new ArrayList<>();
        List<HubEntry> ungrouped = groups.remove("");
        if (ungrouped != null) ordered.addAll(ungrouped);
        groups.values().forEach(ordered::addAll);
        return ordered;
    }

    private static Map<String, HubEntry> builtins() {
        Map<String, HubEntry> entries = new LinkedHashMap<>();
        entries.put("ftbquests", new HubEntry("ftbquests", Component.translatable("entry.liber_ivonis.ftbquests"), Component.translatable("entry.liber_ivonis.ftbquests.description"), () -> FtbQuestsCompat.open(), ModList.get().isLoaded("ftbquests")));
        entries.put("patchouli", new HubEntry("patchouli", Component.translatable("entry.liber_ivonis.patchouli"), Component.translatable("entry.liber_ivonis.patchouli.description"), openCategory("patchouli", "entry.liber_ivonis.patchouli", PatchouliCompat.entries()), ModList.get().isLoaded("patchouli")));
        entries.put("modonomicon", new HubEntry("modonomicon", Component.translatable("entry.liber_ivonis.modonomicon"), Component.translatable("entry.liber_ivonis.modonomicon.description"), openCategory("modonomicon", "entry.liber_ivonis.modonomicon", configured("modonomicon")), ModList.get().isLoaded("modonomicon")));
        entries.put("guideme", new HubEntry("guideme", Component.translatable("entry.liber_ivonis.guideme"), Component.translatable("entry.liber_ivonis.guideme.description"), openCategory("guideme", "entry.liber_ivonis.guideme", GuideMeCompat.entries()), ModList.get().isLoaded("guideme")));
        entries.put("ageratum", new HubEntry("ageratum", Component.translatable("entry.liber_ivonis.ageratum"), Component.translatable("entry.liber_ivonis.ageratum.description"), openCategory("ageratum", "entry.liber_ivonis.ageratum", List.of(new HandbookEntry(Component.translatable("entry.liber_ivonis.ageratum"), AgeratumCompat::open))), ModList.get().isLoaded("ageratum")));
        entries.put("inventory", new HubEntry("inventory", Component.translatable("entry.liber_ivonis.inventory"), Component.translatable("entry.liber_ivonis.inventory.description"), openNative(() -> new InventoryScreen(Minecraft.getInstance().player)), true));
        entries.put("advancement", new HubEntry("advancement", Component.translatable("entry.liber_ivonis.advancement"), Component.translatable("entry.liber_ivonis.advancement.description"), openNative(() -> new AdvancementsScreen(Minecraft.getInstance().player.connection.getAdvancements())), true));
        return entries;
    }

    private static void addConfiguredScreens(List<HubEntry> out) {
        for (String value : IvonisConfig.customScreens.get()) {
            String[] p = value.split("\\|", 4);
            if (p.length < 2) continue;
            String method = p.length >= 3 && !p[2].isBlank() ? p[2] : "create";
            String category = p.length >= 4 && !p[3].isBlank() ? p[3] : "custom";
            out.add(new HubEntry(
                    "custom:" + p[1],
                    configurableTitle(p[0]),
                    Component.translatable("entry.liber_ivonis.configured_screen.description"),
                    () -> openScreen(invokeScreen(p[1], method)),
                    true, category
            ));
        }
    }

    private static void optional(List<HubEntry> out, String mod, String titleKey, String subtitleKey, Supplier<Runnable> opener) {
        boolean available = ModList.get().isLoaded(mod);
        if (available || IvonisConfig.showUnavailable.get()) out.add(new HubEntry(mod, Component.translatable(titleKey), Component.translatable(subtitleKey), opener.get(), available));
    }

    private static Screen useHandbook(String id) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
        return useHandbook(item);
    }

    private static Screen useHandbook(Item item) {
        if (item == BuiltInRegistries.ITEM.get(BuiltInRegistries.ITEM.getDefaultKey())) return null;
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.player != null) {
            client.execute(() -> item.use(client.level, client.player, net.minecraft.world.InteractionHand.MAIN_HAND));
        }
        return null;
    }

    private static Runnable openScreen(Screen screen) {
        return () -> { if (screen != null) Minecraft.getInstance().setScreen(screen); };
    }

    private static Runnable openCategory(String id, String titleKey, List<HandbookEntry> entries) {
        return () -> MuiModApi.openScreen(new HandbookCategoryFragment(Component.translatable(titleKey), entries));
    }

    private static Component configurableTitle(String value) {
        return value.startsWith("key:") && value.length() > 4
                ? Component.translatable(value.substring(4))
                : Component.literal(value);
    }

    private static Runnable openNative(java.util.function.Supplier<Screen> supplier) {
        return () -> {
            Screen screen = supplier.get();
            if (screen != null) HubEntriesHolder.open(screen);
        };
    }

    private static final class HubEntriesHolder {
        private static void open(Screen screen) { Minecraft.getInstance().setScreen(screen); }
    }

    private static List<HandbookEntry> configured(String namespace) {
        List<HandbookEntry> result = new ArrayList<>();
        for (String value : IvonisConfig.handbookItems.get()) {
            String[] p = value.split("\\|", 2);
            if (!p[0].startsWith(namespace + ":")) continue;
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(p[0]));
            if (item == BuiltInRegistries.ITEM.get(BuiltInRegistries.ITEM.getDefaultKey())) continue;
            Component title = p.length > 1 ? configurableTitle(p[1]) : item.getName(item.getDefaultInstance());
            result.add(new HandbookEntry(title, () -> useHandbook(item)));
        }
        return result;
    }

    /**
     * Configuration-only screen bridge. Known integrations must use typed Compat
     * classes; this method is retained for user-configured third-party screens.
     */
    private static Screen invokeScreen(String className, String preferredMethod) {
        try {
            Class<?> type = Class.forName(className);
            for (String methodName : List.of(preferredMethod, "open", "create", "getScreen")) {
                try {
                    Method method = type.getDeclaredMethod(methodName);
                    if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                        method.setAccessible(true);
                        return (Screen) method.invoke(null);
                    }
                } catch (NoSuchMethodException ignored) {
                }
            }
        } catch (ReflectiveOperationException | ClassCastException ignored) {
        }
        return null;
    }

}
