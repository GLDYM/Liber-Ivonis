package com.liberivonis.compat;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/** Optional bridge for Advancements Reloaded's replacement advancement screen. */
public final class AdvancementsReloadedCompat {
    private static final Logger LOGGER = LoggerFactory.getLogger("LiberIvonis/AdvancementsReloaded");
    private static final String SCREEN_CLASS = "codes.atomys.advr.screens.AdvancementReloadedScreen";

    private AdvancementsReloadedCompat() {
    }

    public static Screen create(ClientAdvancements advancements, Screen parent) {
        try {
            Class<?> type = Class.forName(SCREEN_CLASS);
            for (Constructor<?> constructor : type.getConstructors()) {
                Class<?>[] parameters = constructor.getParameterTypes();
                if (parameters.length == 2 && parameters[0].isInstance(advancements)
                        && (parent == null || parameters[1].isInstance(parent))) {
                    return (Screen) constructor.newInstance(advancements, parent);
                }
            }
            Constructor<?> constructor = type.getConstructor(ClientAdvancements.class);
            return (Screen) constructor.newInstance(advancements);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            LOGGER.warn("Unable to create Advancements Reloaded screen; using vanilla screen", e);
            return null;
        }
    }
}
