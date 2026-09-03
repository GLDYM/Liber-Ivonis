package com.liberivonis.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class IvonisConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> customScreens;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> handbookItems;
    /** Ordered ids of the built-in Hub entries. Removing an id hides that entry. */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> builtinEntries;
    /** Optional unified order for built-ins, configured handbook items and custom screens. */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> entryOrder;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("hub");
        builtinEntries = b.comment("Built-in Hub entries, in display order. Format: id|category (category optional). Available ids: ftbquests, patchouli, modonomicon, guideme, ageratum, inventory, advancement")
                .defineListAllowEmpty("builtinEntries", List.of("ftbquests", "patchouli", "modonomicon", "guideme", "ageratum", "inventory", "advancement", "controls"),
                        v -> v instanceof String && !((String) v).isBlank());
        entryOrder = b.comment("Unified display order. Use built-in ids, item:modid:item_id, or custom:fully.qualified.ScreenClass. Unlisted valid entries are appended in their legacy order.")
                .defineListAllowEmpty("entryOrder", List.of(), v -> v instanceof String && !((String) v).isBlank());
        b.pop();
        b.push("handbooks");
        handbookItems = b.comment("Handbook items: modid:item|title|category (prefix category with key: for a translation key)").defineListAllowEmpty("handbookItems", List.of(), v -> v instanceof String && ((String) v).contains(":"));
        b.pop();
        b.push("customScreens");
        customScreens = b.comment("Screens: title|fully.qualified.ScreenClass|method|category (prefix category with key: for a translation key)").defineListAllowEmpty("customScreens", List.of(), v -> v instanceof String && ((String) v).contains("|"));
        b.pop();
        SPEC = b.build();
    }
    private IvonisConfig() {}
}
