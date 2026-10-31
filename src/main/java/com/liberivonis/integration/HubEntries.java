package com.liberivonis.integration;

import com.liberivonis.client.HubLocalization;
import com.liberivonis.client.HubNavigation;
import com.liberivonis.client.screen.HandbookCategoryFragment;
import com.liberivonis.client.screen.HandbookEntry;
import com.liberivonis.compat.FtbQuestsCompat;
import com.liberivonis.compat.GuideMeCompat;
import com.liberivonis.compat.AgeratumCompat;
import com.liberivonis.compat.ModonomiconCompat;
import com.liberivonis.compat.PatchouliCompat;
import com.liberivonis.compat.AdvancementsReloadedCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class HubEntries {
    private static final Logger LOGGER = LoggerFactory.getLogger("LiberIvonis/HubEntries");

    /** Returns a built-in Hub target, or {@code null} when the id is unknown. */
    public static HubEntry builtin(String id) {
        return builtins().get(id);
    }

    public static Map<String, HubEntry> builtins() {
        Map<String, HubEntry> entries = new LinkedHashMap<>();
        boolean inWorld = inWorld();
        entries.put("ftbquests",
                new HubEntry("ftbquests", "entry.liber_ivonis.ftbquests", "FTB Quests",
                        "entry.liber_ivonis.ftbquests.description", "Quest book",
                        () -> FtbQuestsCompat.open(), ModList.get().isLoaded("ftbquests") && inWorld, true));
        boolean patchouliLoaded = ModList.get().isLoaded("patchouli");
        entries.put("patchouli", new HubEntry("patchouli", "entry.liber_ivonis.patchouli", "Patchouli",
                "entry.liber_ivonis.patchouli.description", "Patchouli guide", openCategory("entry.liber_ivonis.patchouli",
                        "Patchouli", patchouliLoaded ? PatchouliCompat.entries() : List.of()),
                patchouliLoaded, false));
        boolean modonomiconLoaded = ModList.get().isLoaded("modonomicon");
        entries.put("modonomicon",
                new HubEntry("modonomicon", "entry.liber_ivonis.modonomicon", "Modonomicon",
                        "entry.liber_ivonis.modonomicon.description", "Modonomicon guide",
                        openCategory("entry.liber_ivonis.modonomicon", "Modonomicon",
                                modonomiconLoaded ? ModonomiconCompat.entries() : List.of()),
                        modonomiconLoaded, false));
        boolean guideMeLoaded = ModList.get().isLoaded("guideme");
        entries.put("guideme",
                new HubEntry("guideme", "entry.liber_ivonis.guideme", "GuideME",
                        "entry.liber_ivonis.guideme.description", "GuideME guide",
                        openCategory("entry.liber_ivonis.guideme", "GuideME",
                                guideMeLoaded ? GuideMeCompat.entries() : List.of()),
                        guideMeLoaded, false));
        boolean ageratumLoaded = ModList.get().isLoaded("ageratum");
        entries.put("ageratum", new HubEntry("ageratum", "entry.liber_ivonis.ageratum", "Ageratum",
                "entry.liber_ivonis.ageratum.description", "Ageratum guide",
                openCategory("entry.liber_ivonis.ageratum", "Ageratum", ageratumLoaded ? List.of(
                        new HandbookEntry(Component.translatable("entry.liber_ivonis.ageratum"), AgeratumCompat::open))
                        : List.of()),
                ageratumLoaded, false));
        entries.put("inventory",
                new HubEntry("inventory", "entry.liber_ivonis.inventory", "Inventory",
                        "entry.liber_ivonis.inventory.description", "View player inventory",
                        openNative(() -> new InventoryScreen(Minecraft.getInstance().player)), inWorld, true));
        entries.put("advancement", new HubEntry("advancement", "entry.liber_ivonis.advancement", "Advancements",
                "entry.liber_ivonis.advancement.description", "View your advancements",
                openNative(HubEntries::createAdvancementScreen),
                inWorld, true));
        entries.put("controls",
                new HubEntry("controls", "entry.liber_ivonis.controls", "Key Bindings",
                        "entry.liber_ivonis.controls.description", "Configure controls",
                        openNative(() -> new KeyBindsScreen(null, Minecraft.getInstance().options)), inWorld, true));
        return entries;
    }

    private static boolean inWorld() {
        Minecraft client = Minecraft.getInstance();
        return client.player != null && client.level != null;
    }

    private static Screen createAdvancementScreen() {
        Minecraft client = Minecraft.getInstance();
        var advancements = client.player.connection.getAdvancements();
        if (ModList.get().isLoaded("advancements_reloaded")) {
            Screen reloaded = AdvancementsReloadedCompat.create(advancements, null);
            if (reloaded != null)
                return reloaded;
        }
        return new AdvancementsScreen(advancements);
    }

    public static HubEntry screen(String className, String method, String titleKey, String titleFallback,
            String descriptionKey, String descriptionFallback) {
        if (className == null || className.isBlank())
            throw new IllegalArgumentException("Screen class cannot be blank");
        String actualMethod = method == null || method.isBlank() ? "create" : method.trim();
        String actualTitle = titleFallback == null || titleFallback.isBlank()
                ? className.substring(className.lastIndexOf('.') + 1)
                : titleFallback;
        return new HubEntry(
                "custom:" + className + "#" + actualMethod,
                titleKey == null ? "" : titleKey,
                actualTitle,
                descriptionKey == null || descriptionKey.isBlank()
                        ? "entry.liber_ivonis.configured_screen.description"
                        : descriptionKey,
                descriptionFallback == null || descriptionFallback.isBlank()
                        ? "Configured screen"
                        : descriptionFallback,
                () -> openScreen(invokeScreen(className, actualMethod)).run(),
                true, false);
    }

    public static Optional<HubEntry> item(String itemId, String titleKey, String titleFallback,
            String descriptionKey, String descriptionFallback) {
        final ResourceLocation location;
        try {
            location = ResourceLocation.parse(itemId);
        } catch (RuntimeException exception) {
            LOGGER.warn("Ignoring Hub entry with invalid item id '{}'", itemId, exception);
            return Optional.empty();
        }
        Item item = BuiltInRegistries.ITEM.get(location);
        if (item == BuiltInRegistries.ITEM.get(BuiltInRegistries.ITEM.getDefaultKey())) {
            LOGGER.warn("Ignoring Hub entry because item '{}' is not registered", location);
            return Optional.empty();
        }
        String fallback = titleFallback == null || titleFallback.isBlank()
                ? item.getName(item.getDefaultInstance()).getString()
                : titleFallback;
        return Optional.of(new HubEntry("item:" + location,
                titleKey == null ? "" : titleKey,
                fallback,
                descriptionKey == null || descriptionKey.isBlank()
                        ? "entry.liber_ivonis.configured_handbook.description"
                        : descriptionKey,
                descriptionFallback == null || descriptionFallback.isBlank()
                        ? "Configured handbook"
                        : descriptionFallback,
                () -> useHandbook(item), true, true));
    }

    private static Screen useHandbook(Item item) {
        if (item == BuiltInRegistries.ITEM.get(BuiltInRegistries.ITEM.getDefaultKey()))
            return null;
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.player != null) {
            client.execute(() -> item.use(client.level, client.player, net.minecraft.world.InteractionHand.MAIN_HAND));
        }
        return null;
    }

    private static Runnable openScreen(Screen screen) {
        return () -> {
            if (screen != null) {
                Minecraft.getInstance().setScreen(screen);
            } else {
                LOGGER.warn("Custom screen resolved to null");
            }
        };
    }

    private static Runnable openCategory(String titleKey, String fallback, List<HandbookEntry> entries) {
        return () -> HubNavigation.openModernScreen(new HandbookCategoryFragment(
                HubLocalization.current().component(titleKey, fallback), entries));
    }

    private static Runnable openNative(java.util.function.Supplier<Screen> supplier) {
        return () -> {
            Screen screen = supplier.get();
            if (screen != null)
                HubEntriesHolder.open(screen);
        };
    }

    private static final class HubEntriesHolder {
        private static void open(Screen screen) {
            Minecraft.getInstance().setScreen(screen);
        }
    }

    /**
     * Configuration-only screen bridge. Known integrations must use typed Compat
     * classes; this method is retained for user-configured third-party screens.
     */
    private static Screen invokeScreen(String className, String preferredMethod) {
        try {
            Class<?> type = Class.forName(className);
            // LOGGER.info("Resolving custom screen {} using {}", className,
            // preferredMethod);
            Minecraft client = Minecraft.getInstance();
            Object[] commonArgs = { client, client.level, client.player, null };
            boolean constructorOnly = "<init>".equals(preferredMethod);
            for (String methodName : constructorOnly ? List.<String>of()
                    : List.of(preferredMethod, "open", "create", "getScreen")) {
                for (Method method : type.getDeclaredMethods()) {
                    if (!method.getName().equals(methodName)
                            || !java.lang.reflect.Modifier.isStatic(method.getModifiers()))
                        continue;
                    Object[] args = resolveArguments(method.getParameterTypes(), commonArgs);
                    if (args == null)
                        continue;
                    // LOGGER.info("Invoking {}.{} with {} parameter(s)", className, methodName,
                    // args.length);
                    method.setAccessible(true);
                    Object result = method.invoke(null, args);
                    if (result instanceof Screen screen)
                        return screen;
                }
            }
            // Also support constructors such as new KeymapScreen(null).
            java.lang.reflect.Constructor<?>[] constructors = type.getDeclaredConstructors();
            // LOGGER.info("Inspecting {} constructor(s) for {}", constructors.length,
            // className);
            for (java.lang.reflect.Constructor<?> constructor : constructors) {
                Object[] args = resolveArguments(constructor.getParameterTypes(), commonArgs);
                if (args == null) {
                    LOGGER.warn("Skipping unsupported constructor {}", constructor);
                    continue;
                }
                // LOGGER.info("Invoking {} constructor with {} parameter(s)", className,
                // args.length);
                constructor.setAccessible(true);
                Object result = constructor.newInstance(args);
                // LOGGER.info("Constructed custom screen object: {}", result == null ? "null" :
                // result.getClass().getName());
                if (result instanceof Screen screen)
                    return screen;
            }
            // LOGGER.info("No compatible constructor found for {}. Available constructors:
            // {}",
            // className, java.util.Arrays.toString(type.getDeclaredConstructors()));
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            LOGGER.error("Failed to create custom screen {} using {}", className, preferredMethod, e);
        }
        return null;
    }

    private static Object[] resolveArguments(Class<?>[] parameterTypes, Object[] commonArgs) {
        Object[] result = new Object[parameterTypes.length];
        boolean[] used = new boolean[commonArgs.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            boolean found = false;
            for (int j = 0; j < commonArgs.length; j++) {
                Object value = commonArgs[j];
                if (used[j] || (value != null && !parameterTypes[i].isInstance(value)))
                    continue;
                if (value == null && parameterTypes[i].isPrimitive())
                    continue;
                result[i] = value;
                used[j] = true;
                found = true;
                break;
            }
            if (!found)
                return null;
        }
        return result;
    }

}
