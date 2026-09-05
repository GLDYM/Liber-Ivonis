package com.liberivonis.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class IvonisConfig {
    public static final ModConfigSpec SPEC;
    /** One ordered list containing built-ins, item handbooks and custom screens. */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> hubEntries;
    /** Colours shared by the Hub and its handbook category pages. */
    public static final ModConfigSpec.ConfigValue<String> hubBackground;
    public static final ModConfigSpec.ConfigValue<String> hubSurface;
    public static final ModConfigSpec.ConfigValue<String> hubSurfaceAlt;
    public static final ModConfigSpec.ConfigValue<String> hubAccent;
    public static final ModConfigSpec.ConfigValue<String> hubMuted;
    public static final ModConfigSpec.ConfigValue<String> hubOutline;
    public static final ModConfigSpec.ConfigValue<String> hubDisabledOutline;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("hub");
        hubEntries = builder.comment(
                "Hub entries in display order. Built-in: id|category. Handbook: item:modid:item_id|title|category. Screen: screen:title|class|method|category")
                .defineListAllowEmpty("entries",
                        () -> List.of("ftbquests", "patchouli", "modonomicon", "guideme", "ageratum", "inventory",
                                "advancement", "controls"),
                        () -> "screen:title|class|method|category",
                        v -> v instanceof String && !((String) v).isBlank());
        builder.push("colors");
        builder.comment("Hub colours in #AARRGGBB or #RRGGBB hexadecimal format. #RRGGBB is fully opaque.");
        hubBackground = builder.define("background", "#FF0D1117", IvonisConfig::isArgbColor);
        hubSurface = builder.define("surface", "#FF171D27", IvonisConfig::isArgbColor);
        hubSurfaceAlt = builder.define("surfaceAlt", "#FF202938", IvonisConfig::isArgbColor);
        hubAccent = builder.define("accent", "#FFE8D9B5", IvonisConfig::isArgbColor);
        hubMuted = builder.define("muted", "#FF9AA6B8", IvonisConfig::isArgbColor);
        hubOutline = builder.define("outline", "#FF364255", IvonisConfig::isArgbColor);
        hubDisabledOutline = builder.define("disabledOutline", "#FF252C38", IvonisConfig::isArgbColor);
        builder.pop();
        builder.pop();
        SPEC = builder.build();
    }

    private static boolean isArgbColor(Object value) {
        if (!(value instanceof String color))
            return false;
        return color.matches("#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?");
    }

    private IvonisConfig() {
    }
}
